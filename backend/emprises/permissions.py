from rest_framework.permissions import BasePermission

class IsEmpresaOwner(BasePermission):
    """
    Permite sólo si el usuario es el owner de la empresa OR si su role.code == 'ADMIN' y pertenezca a la misma empresa.
    Ajusta según la lógica que prefieras.
    """
    def has_object_permission(self, request, view, obj):
        # obj es Empresa
        if not request.user or not request.user.is_authenticated:
            return False
        # owner (creador) tiene permiso completo
        if obj.owner_id == request.user.id:
            return True
        # si user tiene role y pertenece a la misma empresa (edge case)
        user_empresa = getattr(request.user, 'empresa_id', None)
        if user_empresa and user_empresa == obj.id:
            # permitimos si es ADMIN of that company
            role = getattr(request.user, 'role', None)
            if role and getattr(role, 'code', None) == 'ADMIN':
                return True
        return False
