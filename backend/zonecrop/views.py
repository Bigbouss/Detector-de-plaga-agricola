from rest_framework import viewsets, mixins, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from django.shortcuts import get_object_or_404
from django.db.models import Prefetch
from django.db import transaction

from .models import Zona, Cultivo, WorkerZoneAssignment
from .serializers import ZonaSerializer, CultivoSerializer, WorkerZoneAssignmentSerializer
from .permissions import IsCompanyAdmin, IsAssignedWorker
from accounts.models import User

class ZonaViewSet(viewsets.ModelViewSet):
    serializer_class = ZonaSerializer
    permission_classes = [IsAuthenticated, IsAssignedWorker]

    def get_queryset(self):
        user = self.request.user
        if getattr(user, 'role', None) and user.role.code == 'ADMIN':
            return Zona.objects.filter(empresa=user.empresa).prefetch_related('cultivos')
        assigned_zone_ids = user.zone_assignments.values_list('zona_id', flat=True)
        return Zona.objects.filter(pk__in=assigned_zone_ids).prefetch_related('cultivos')

    def perform_create(self, serializer):
        user = self.request.user
        if not (user.role and user.role.code == 'ADMIN'):
            return Response({"detail": "No tienes permiso para crear zonas."}, status=status.HTTP_403_FORBIDDEN)
        serializer.save(empresa=user.empresa)

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [IsAuthenticated(), IsCompanyAdmin()]
        return [IsAuthenticated(), IsAssignedWorker()]


class CultivoViewSet(viewsets.ModelViewSet):
    serializer_class = CultivoSerializer

    def get_queryset(self):
        user = self.request.user
        if getattr(user, 'role', None) and user.role.code == 'ADMIN':
            return Cultivo.objects.filter(zona__empresa=user.empresa).select_related('zona')
        assigned_zone_ids = user.zone_assignments.values_list('zona_id', flat=True)
        return Cultivo.objects.filter(zona_id__in=assigned_zone_ids).select_related('zona')

    def perform_create(self, serializer):
        user = self.request.user
        if not (user.role and user.role.code == 'ADMIN'):
            return Response({"detail": "No tienes permiso para crear cultivos."}, status=status.HTTP_403_FORBIDDEN)
        zona = serializer.validated_data.get('zona')
        if zona.empresa_id != user.empresa_id:
            return Response({"detail": "La zona no pertenece a tu empresa."}, status=status.HTTP_400_BAD_REQUEST)
        serializer.save()


class WorkerZoneAssignmentViewSet(viewsets.ModelViewSet):
    """
    Admin asigna workers a zonas.
    """
    serializer_class = WorkerZoneAssignmentSerializer
    permission_classes = [IsAuthenticated, IsCompanyAdmin]  # Default para CRUD

    def get_queryset(self):
        return WorkerZoneAssignment.objects.filter(
            zona__empresa=self.request.user.empresa
        ).select_related('worker', 'zona')

    #Sobrescribir permisos para acciones específicas
    def get_permissions(self):
        # Workers pueden consultar sus propias zonas
        if self.action == 'worker_zones':
            return [IsAuthenticated()]
        # Solo admins para el resto
        return [IsAuthenticated(), IsCompanyAdmin()]

    @action(detail=False, methods=['post'], url_path='assign-worker-zones')
    @transaction.atomic
    def assign_worker_zones(self, request):
        """
        POST /api/zonecrop/assignments/assign-worker-zones/
        Solo admins
        """
        worker_id = request.data.get('worker_id')
        zone_ids = request.data.get('zone_ids', [])

        if not worker_id:
            return Response(
                {'detail': 'worker_id es requerido'},
                status=status.HTTP_400_BAD_REQUEST
            )

        if not isinstance(zone_ids, list):
            return Response(
                {'detail': 'zone_ids debe ser una lista'},
                status=status.HTTP_400_BAD_REQUEST
            )

        # Verificar que el worker pertenece a la empresa del admin
        try:
            worker = User.objects.get(
                pk=worker_id,
                empresa=request.user.empresa,
                role__code='WORKER'
            )
        except User.DoesNotExist:
            return Response(
                {'detail': 'Trabajador no encontrado o no pertenece a tu empresa'},
                status=status.HTTP_404_NOT_FOUND
            )

        # Verificar que todas las zonas pertenecen a la empresa
        zonas = Zona.objects.filter(
            pk__in=zone_ids,
            empresa=request.user.empresa
        )

        if zonas.count() != len(zone_ids):
            return Response(
                {'detail': 'Algunas zonas no existen o no pertenecen a tu empresa'},
                status=status.HTTP_400_BAD_REQUEST
            )

        # Eliminar asignaciones anteriores
        WorkerZoneAssignment.objects.filter(worker=worker).delete()

        # Crear nuevas asignaciones
        assignments = [
            WorkerZoneAssignment(worker=worker, zona=zona)
            for zona in zonas
        ]
        WorkerZoneAssignment.objects.bulk_create(assignments)

        return Response({
            'message': f'{len(zone_ids)} zonas asignadas a {worker.email}',
            'worker_id': worker.id,
            'assigned_zone_ids': zone_ids
        }, status=status.HTTP_200_OK)

    @action(detail=False, methods=['get'], url_path='worker-zones/(?P<worker_id>[^/.]+)')
    def worker_zones(self, request, worker_id=None):
        """
        GET /api/zonecrop/assignments/worker-zones/{worker_id}/
        Workers pueden consultar sus propias zonas
        Admins pueden consultar las de cualquier worker de su empresa
        """
        try:
            # Si es worker, solo puede ver sus propias zonas
            if request.user.role.code == 'WORKER':
                if str(request.user.id) != str(worker_id):
                    return Response(
                        {'detail': 'No tienes permiso para ver las zonas de otro trabajador'},
                        status=status.HTTP_403_FORBIDDEN
                    )
                worker = request.user
            else:
                # Si es admin, puede ver cualquier worker de su empresa
                worker = User.objects.get(
                    pk=worker_id,
                    empresa=request.user.empresa,
                    role__code='WORKER'
                )
        except User.DoesNotExist:
            return Response(
                {'detail': 'Trabajador no encontrado'},
                status=status.HTTP_404_NOT_FOUND
            )

        zone_ids = WorkerZoneAssignment.objects.filter(
            worker=worker
        ).values_list('zona_id', flat=True)

        return Response({
            'worker_id': worker.id,
            'zone_ids': list(zone_ids)
        }, status=status.HTTP_200_OK)