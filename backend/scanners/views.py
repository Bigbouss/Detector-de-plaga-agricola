# scanners/views.py
from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from rest_framework.decorators import action
from rest_framework.parsers import MultiPartParser, FormParser

from django.http import Http404
from django.utils import timezone

import boto3
import uuid

from django.conf import settings
from django.db import transaction

from .models import ModelVersion, ScannerSession, ScannerImage, ScannerResult
from .serializers import (
    ModelVersionSerializer, ScannerSessionSerializer,
    ScannerImageSerializer, ScannerResultSerializer,
    ScannerSessionWithReportSerializer,
    ScannerSessionCreateSerializer,
    ScanResultCreateSerializer
)


# -------------------------------------------------------------------
#   AWS S3 CLIENT (usa credenciales desde settings / env)
# -------------------------------------------------------------------
s3_client = boto3.client(
    's3',
    region_name=getattr(settings, "AWS_S3_REGION_NAME", None),
    aws_access_key_id=getattr(settings, "AWS_ACCESS_KEY_ID", None),
    aws_secret_access_key=getattr(settings, "AWS_SECRET_ACCESS_KEY", None),
)


# -------------------------------------------------------------------
#   MODEL VERSION VIEWSET
# -------------------------------------------------------------------
class ModelVersionViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = ModelVersion.objects.all().order_by('-created_at')
    serializer_class = ModelVersionSerializer
    permission_classes = [IsAuthenticated]


# -------------------------------------------------------------------
#   SCANNER SESSION VIEWSET
# -------------------------------------------------------------------
class ScannerSessionViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    queryset = ScannerSession.objects.all()

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'empresa'):
            return ScannerSession.objects.filter(empresa=user.empresa).order_by('-started_at')
        return ScannerSession.objects.none()

    def get_serializer_class(self):
        if self.action == 'create':
            return ScannerSessionCreateSerializer
        elif self.action == 'with_reports':
            return ScannerSessionWithReportSerializer
        return ScannerSessionSerializer

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)

    @action(detail=False, methods=['get'])
    def with_reports(self, request):
        user = request.user
        if not hasattr(user, 'empresa'):
            return Response([])

        sessions = ScannerSession.objects.filter(
            empresa=user.empresa,
            status='COMPLETED'
        ).select_related('session_report', 'empresa', 'zona', 'cultivo').order_by('-finished_at')

        worker_name = request.query_params.get('worker')
        if worker_name:
            sessions = sessions.filter(worker_name__icontains=worker_name)

        date_from = request.query_params.get('date_from')
        if date_from:
            sessions = sessions.filter(finished_at__gte=date_from)

        date_to = request.query_params.get('date_to')
        if date_to:
            sessions = sessions.filter(finished_at__lte=date_to)

        serializer = ScannerSessionWithReportSerializer(sessions, many=True)
        return Response(serializer.data)

    @action(detail=True, methods=['post'])
    def finish(self, request, pk=None):
        session = self.get_object()
        if session.status != 'ACTIVE':
            return Response({"error": "La sesión no está activa"}, status=status.HTTP_400_BAD_REQUEST)
        session.status = 'COMPLETED'
        session.finished_at = timezone.now()
        session.notes = request.data.get('notes', session.notes)
        session.save()
        return Response(ScannerSessionSerializer(session).data)

    @action(detail=False, methods=['post'])
    def sync(self, request):
        sessions_data = request.data.get('sessions', [])
        if not sessions_data:
            return Response({"error": "Se requiere una lista de sesiones"}, status=status.HTTP_400_BAD_REQUEST)

        synced = []
        errors = []

        for session_data in sessions_data:
            try:
                with transaction.atomic():
                    session_id = session_data['session_id']
                    scan_results_data = session_data.get('scan_results', [])

                    total_scans = len(scan_results_data)
                    healthy_count = sum(1 for r in scan_results_data if not r.get('has_plague'))
                    plague_count = sum(1 for r in scan_results_data if r.get('has_plague'))

                    session, created = ScannerSession.objects.update_or_create(
                        session_id=session_id,
                        defaults={
                            'empresa_id': session_data['empresa_id'],
                            'zona_id': session_data['zona_id'],
                            'cultivo_id': session_data['cultivo_id'],
                            'owner': request.user,
                            'worker_name': session_data['worker_name'],
                            'model_version_string': session_data['model_version_string'],
                            'started_at': session_data['started_at'],
                            'finished_at': session_data.get('finished_at'),
                            'status': session_data['status'],
                            'total_scans': total_scans,
                            'healthy_count': healthy_count,
                            'plague_count': plague_count,
                            'notes': session_data.get('notes', ''),
                        }
                    )

                    for result_data in scan_results_data:
                        ScannerResult.objects.update_or_create(
                            result_id=result_data['result_id'],
                            defaults={
                                'session': session,
                                'photo_path': result_data['photo_path'],
                                'classification': result_data['classification'],
                                'confidence': result_data['confidence'],
                                'has_plague': result_data['has_plague'],
                                'report_id': result_data.get('report_id'),
                                'scanned_at': result_data['scanned_at'],
                            }
                        )

                    synced.append({'session_id': session_id, 'created': created, 'scan_results_count': len(scan_results_data)})

            except Exception as e:
                errors.append({'session_id': session_data.get('session_id', 'unknown'), 'error': str(e)})

        return Response({'synced': synced, 'errors': errors, 'total_synced': len(synced), 'total_errors': len(errors)})


    @action(detail=True, methods=['post'])
    def cancel(self, request, pk=None):
        session = self.get_object()
        if session.status != 'ACTIVE':
            return Response({"error": "La sesión no está activa"}, status=status.HTTP_400_BAD_REQUEST)
        session.status = 'CANCELLED'
        session.finished_at = timezone.now()
        session.save()
        return Response(ScannerSessionSerializer(session).data)


# -------------------------------------------------------------------
#   SCANNER IMAGE VIEWSET
# -------------------------------------------------------------------
class ScannerImageViewSet(viewsets.ModelViewSet):
    serializer_class = ScannerImageSerializer
    permission_classes = [IsAuthenticated]
    queryset = ScannerImage.objects.all()

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'empresa'):
            return ScannerImage.objects.filter(session__empresa=user.empresa)
        return ScannerImage.objects.none()


# -------------------------------------------------------------------
#   SCANNER RESULT VIEWSET (S3 - PUBLIC)
# -------------------------------------------------------------------
class ScannerResultViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    queryset = ScannerResult.objects.all()

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'empresa'):
            return ScannerResult.objects.filter(session__empresa=user.empresa).order_by('-scanned_at')
        return ScannerResult.objects.none()

    def get_serializer_class(self):
        if self.action == 'create':
            return ScanResultCreateSerializer
        return ScannerResultSerializer

    @action(detail=True, methods=['get'])
    def image(self, request, pk=None):
        """
        Devuelve la URL pública directa a la imagen en S3 (public-read).
        GET /api/scanners/results/{result_id}/image/
        """
        result = self.get_object()
        if not result.photo_path:
            raise Http404("No hay imagen disponible")

        # URL pública construida (usando CUSTOM_DOMAIN o bucket.s3.amazonaws.com)
        domain = getattr(settings, "AWS_S3_CUSTOM_DOMAIN", f"{settings.AWS_STORAGE_BUCKET_NAME}.s3.amazonaws.com")
        public_url = f"https://{domain}/{result.photo_path}"
        return Response({"image_url": public_url})

    @action(detail=True, methods=['post'], parser_classes=[MultiPartParser, FormParser], url_path='upload-image')
    def upload_image(self, request, pk=None):
        """
        Sube la imagen recibida al bucket S3 en path:
        scanner_photos/{session.session_id}/{result.result_id}.jpg
        - ACL: public-read (tal como pediste para pruebas)
        - Guarda el key en ScannerResult.photo_path (CharField)
        """
        result = self.get_object()

        if 'image' not in request.FILES:
            return Response({"error": "No se proporcionó ninguna imagen"}, status=status.HTTP_400_BAD_REQUEST)

        image_file = request.FILES['image']

        # Extensión y content type (intenta inferir)
        content_type = getattr(image_file, "content_type", "image/jpeg")
        ext = "jpg"
        # genera nombre único (puedes mantener result.result_id como nombre)
        key = f"scanner_photos/{result.session.session_id}/{result.result_id}.{ext}"

        try:
            s3_client.upload_fileobj(
                image_file,
                settings.AWS_STORAGE_BUCKET_NAME,
                key,
                ExtraArgs={'ACL': 'public-read', 'ContentType': content_type}
            )
        except Exception as e:
            return Response({"error": f"Error subiendo a S3: {str(e)}"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        # Guardamos el key en el campo photo_path (CharField) — kotlin lo consumirá tal cual
        result.photo_path = key
        result.save()

        return Response(ScannerResultSerializer(result, context={'request': request}).data)

    @action(detail=False, methods=['post'])
    def sync(self, request):
        results_data = request.data.get('results', [])
        if not results_data:
            return Response({"error": "Se requiere una lista de resultados"}, status=status.HTTP_400_BAD_REQUEST)

        synced = []
        errors = []

        for result_data in results_data:
            try:
                session_id = result_data.get('session')
                try:
                    session = ScannerSession.objects.get(session_id=session_id)
                except ScannerSession.DoesNotExist:
                    errors.append({'result_id': result_data.get('result_id'), 'error': f"Sesión {session_id} no encontrada"})
                    continue

                result, created = ScannerResult.objects.update_or_create(
                    result_id=result_data['result_id'],
                    defaults={
                        'session': session,
                        'photo_path': result_data['photo_path'],
                        'classification': result_data['classification'],
                        'confidence': result_data['confidence'],
                        'has_plague': result_data['has_plague'],
                        'report_id': result_data.get('report_id'),
                        'scanned_at': result_data['scanned_at'],
                    }
                )

                synced.append({'result_id': result_data['result_id'], 'created': created})

            except Exception as e:
                errors.append({'result_id': result_data.get('result_id'), 'error': str(e)})

        return Response({'synced': synced, 'errors': errors, 'total_synced': len(synced), 'total_errors': len(errors)})
