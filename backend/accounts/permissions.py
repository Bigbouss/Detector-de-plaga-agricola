# accounts/permissions.py
from rest_framework.permissions import BasePermission

class IsAdmin(BasePermission):
    message = "Requiere rol ADMIN."

    def has_permission(self, request, view):
        u = request.user
        return bool(u and u.is_authenticated and getattr(u, 'role', None) and u.role.code == 'ADMIN')


class IsWorker(BasePermission):
    message = "Requiere rol WORKER."

    def has_permission(self, request, view):
        u = request.user
        return bool(u and u.is_authenticated and getattr(u, 'role', None) and u.role.code == 'WORKER')
