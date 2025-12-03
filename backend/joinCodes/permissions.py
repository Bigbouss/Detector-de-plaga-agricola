from rest_framework.permissions import BasePermission

class IsCompanyAdmin(BasePermission):
    """Permite acceso sólo a administradores de empresa."""
    def has_permission(self, request, view):
        return (
            bool(request.user and request.user.is_authenticated)
            and hasattr(request.user, 'role')
            and getattr(request.user.role, 'code', None) == 'ADMIN'
        )
