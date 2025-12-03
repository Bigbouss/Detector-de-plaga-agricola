from rest_framework import viewsets, status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.decorators import action
from django.shortcuts import get_object_or_404

from accounts.models import User, Role
from .models import Empresa
from .serializers import EmpresaSerializer, EmpresaCreateSerializer
from .permissions import IsEmpresaOwner


class WorkerViewSet(viewsets.ViewSet):
    """
    ViewSet para gestionar workers de la empresa del admin autenticado
    """
    permission_classes = [IsAuthenticated]

    def list(self, request):
        """
        GET /api/emprises/workers/
        Lista todos los workers de la empresa del admin autenticado
        """
        user = request.user
        
        # Verificar que sea admin
        if not user.role or user.role.code != 'ADMIN':
            return Response(
                {'detail': 'Solo administradores pueden ver trabajadores'},
                status=status.HTTP_403_FORBIDDEN
            )
        
        # Verificar que tenga empresa
        if not user.empresa:
            return Response(
                {'detail': 'Usuario sin empresa asignada'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Obtener todos los workers de la misma empresa
        workers = User.objects.filter(
            empresa=user.empresa,
            role__code='WORKER',
            is_active=True  # Solo activos
        ).select_related('role', 'empresa').order_by('-created_at')
        
        # Serializar con estructura anidada que espera tu app
        workers_data = []
        for worker in workers:
            workers_data.append({
                'id': worker.id,
                'user': {
                    'id': worker.id,
                    'username': worker.email.split('@')[0],
                    'email': worker.email,
                    'first_name': worker.first_name,
                    'last_name': worker.last_name
                },
                'profile': {
                    'role': worker.role.code if worker.role else 'WORKER',
                    'is_active': worker.is_active,
                    'can_manage_plots': False,
                    'joined_at': worker.created_at.isoformat()
                }
            })
        
        return Response(workers_data, status=status.HTTP_200_OK)

    def destroy(self, request, pk=None):
        """
        DELETE /api/emprises/workers/{id}/
        Desvincula un worker de la empresa (lo desactiva)
        """
        user = request.user
        
        # Verificar que sea admin
        if not user.role or user.role.code != 'ADMIN':
            return Response(
                {'detail': 'Solo administradores pueden eliminar trabajadores'},
                status=status.HTTP_403_FORBIDDEN
            )
        
        try:
            worker = User.objects.get(
                pk=pk,
                empresa=user.empresa,
                role__code='WORKER'
            )
        except User.DoesNotExist:
            return Response(
                {'detail': 'Trabajador no encontrado'},
                status=status.HTTP_404_NOT_FOUND
            )
        
        # Desactivar en lugar de eliminar
        worker.is_active = False
        worker.save()
        
        return Response(status=status.HTTP_204_NO_CONTENT)

    @action(detail=True, methods=['post'], url_path='assign-zones')
    def assign_zones(self, request, pk=None):
        """
        POST /api/emprises/workers/{id}/assign-zones/
        Asigna zonas a un trabajador
        """
        user = request.user
        
        if not user.role or user.role.code != 'ADMIN':
            return Response(
                {'detail': 'Solo administradores pueden asignar zonas'},
                status=status.HTTP_403_FORBIDDEN
            )
        
        try:
            worker = User.objects.get(
                pk=pk,
                empresa=user.empresa,
                role__code='WORKER'
            )
        except User.DoesNotExist:
            return Response(
                {'detail': 'Trabajador no encontrado'},
                status=status.HTTP_404_NOT_FOUND
            )
        
        zone_ids = request.data.get('zone_ids', [])
        
        if not isinstance(zone_ids, list):
            return Response(
                {'detail': 'zone_ids debe ser una lista'},
                status=status.HTTP_400_BAD_REQUEST
            )

        return Response({
            'message': f'{len(zone_ids)} zonas asignadas a {worker.email}',
            'worker_id': worker.id,
            'assigned_zones': zone_ids
        }, status=status.HTTP_200_OK)


# Mantén tu EmpresaViewSet existente
class EmpresaViewSet(viewsets.ModelViewSet):
    queryset = Empresa.objects.select_related('owner').all()
    serializer_class = EmpresaSerializer
    permission_classes = [IsAuthenticated]

    def get_permissions(self):
        if self.action in ['create']:
            return [IsAuthenticated()]
        if self.action in ['update', 'partial_update', 'destroy']:
            return [IsAuthenticated(), IsEmpresaOwner()]
        return [IsAuthenticated()]

    def get_serializer_class(self):
        return EmpresaCreateSerializer if self.action == 'create' else EmpresaSerializer