"""
cropcare_orgs/permissions.py
Permisos personalizados para la aplicación de organizaciones.
"""
from rest_framework.permissions import BasePermission

class IsCompanyAdmin(BasePermission):
    message = 'Sólo administradores de empresa pueden realizar esta acción.'

    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated:
            return False
        profile = getattr(user, 'profile', None)
        return bool(profile and profile.role == 'ADMIN' and profile.empresa_id)

class IsCompanyMember(BasePermission):
    message = 'Debes pertenecer a una empresa (ADMIN o WORKER) para realizar esta acción.'

    def has_permission(self, request, view):
        u = request.user
        if not u or not u.is_authenticated:
            return False
        p = getattr(u, 'profile', None)
        return bool(p and p.empresa_id and p.role in ('ADMIN', 'WORKER'))
