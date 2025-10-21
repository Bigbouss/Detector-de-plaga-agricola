"""
api/permissions.py
Permisos personalizados para la creación/edición de parcelas y zonas en la app API.
"""
from rest_framework.permissions import BasePermission, SAFE_METHODS


class CanCreateZona(BasePermission):
    """
    Permite crear/editar zonas solo a usuarios ADMIN o WORKER con permiso de gestión de parcelas.
    Lectura de zonas solo para miembros de empresa.
    """
    message = 'No tienes permiso para crear o modificar zonas.'

    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated:
            return False
        profile = getattr(user, 'profile', None)
        # Solo miembros de empresa pueden ver zonas
        if request.method in SAFE_METHODS:
            return bool(profile and profile.empresa_id)
        # Métodos de escritura (POST, PUT, PATCH, DELETE)
        if not profile or not profile.empresa_id:
            return False
        if profile.role == 'ADMIN':
            return True
        if profile.role == 'WORKER' and profile.can_manage_plots:
            return True
        return False


class CanCreatePlot(BasePermission):
    """
    Permite crear/editar parcelas solo a usuarios ADMIN o WORKER con permiso de gestión,
    o a usuarios individuales (para sus propias parcelas). Lectura permitida a todos los usuarios autenticados.
    """
    message = 'No tienes permiso para crear o modificar parcelas.'

    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated:
            return False
        profile = getattr(user, 'profile', None)
        # Todos los usuarios autenticados pueden leer (listar/obtener) parcelas accesibles
        if request.method in SAFE_METHODS:
            return True
        # Métodos de escritura
        if profile and profile.empresa_id:
            # Usuario de empresa
            if profile.role == 'ADMIN':
                return True
            if profile.role == 'WORKER' and profile.can_manage_plots:
                return True
            return False
        else:
            # Usuario individual (sin empresa): puede crear/editar sus propias parcelas
            return True
