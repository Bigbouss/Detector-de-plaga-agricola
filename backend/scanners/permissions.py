from rest_framework.permissions import BasePermission

class IsWorkerOrAdmin(BasePermission):
    """Permite acceso solo a usuarios autenticados de la misma empresa."""
    def has_object_permission(self, request, view, obj):
        user_empresa = getattr(request.user, 'empresa', None)
        obj_empresa = getattr(obj, 'empresa', None)
        return request.user.is_authenticated and user_empresa == obj_empresa
