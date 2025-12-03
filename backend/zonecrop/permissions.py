from rest_framework.permissions import BasePermission, SAFE_METHODS

class IsCompanyAdmin(BasePermission):
    """
    Permite si request.user pertenece a la misma empresa y tiene rol ADMIN.
    Se asume que user.role.code existe y user.empresa está seteado.
    """
    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated:
            return False
        role = getattr(user, 'role', None)
        return bool(role and getattr(role, 'code', None) == 'ADMIN' and getattr(user, 'empresa', None) is not None)

    def has_object_permission(self, request, view, obj):
        # obj puede ser Empresa, Zona, Cultivo: comprobar empresa relacionada
        user = request.user
        if not user or not user.is_authenticated:
            return False
        empresa_id = getattr(user, 'empresa_id', None)
        # obtener empresa del objeto
        obj_empresa_id = None
        if hasattr(obj, 'empresa_id'):
            obj_empresa_id = obj.empresa_id
        elif hasattr(obj, 'zona') and getattr(obj, 'zona', None) is not None:
            obj_empresa_id = obj.zona.empresa_id
        return empresa_id is not None and obj_empresa_id == empresa_id


class IsAssignedWorker(BasePermission):
    """
    Permite acceso a trabajadores sólo a objetos (zona/cultivo) a los que están asignados.
    Lectura permitida si:
      - user es ADMIN de la misma empresa OR
      - user está asignado a la zona (WorkerZoneAssignment)
    Escritura (POST/PUT/PATCH/DELETE) normalmente solo ADMIN.
    """
    def has_permission(self, request, view):
        # cualquiera autenticado puede probar list/detail; object-level decide
        return bool(request.user and request.user.is_authenticated)

    def has_object_permission(self, request, view, obj):
        user = request.user
        # Admin de empresa tiene acceso completo
        role = getattr(user, 'role', None)
        if role and getattr(role, 'code', None) == 'ADMIN' and getattr(user, 'empresa_id', None) == getattr(obj, 'empresa_id', getattr(getattr(obj, 'zona', None), 'empresa_id', None)):
            return True
        # Worker: comprobar asignación
        # si obj es Zona:
        if hasattr(obj, 'id') and obj.__class__.__name__ == 'Zona':
            return obj.worker_assignments.filter(worker_id=user.id).exists()
        # si obj es Cultivo:
        if hasattr(obj, 'zona'):
            return obj.zona.worker_assignments.filter(worker_id=user.id).exists()
        return False
