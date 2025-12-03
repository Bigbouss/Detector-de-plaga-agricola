from django.db.models.signals import post_migrate
from django.dispatch import receiver
from django.apps import apps

@receiver(post_migrate)
def create_default_roles(sender, **kwargs):
    """
    Crea las filas Role('ADMIN') y Role('WORKER') después de las migraciones.
    Se ejecuta SOLO cuando el app 'accounts' termina migraciones.
    """
    if sender.name != 'accounts':
        return

    Role = apps.get_model('accounts', 'Role')
    Role.objects.get_or_create(code='ADMIN', defaults={'name': 'Administrador'})
    Role.objects.get_or_create(code='WORKER', defaults={'name': 'Trabajador'})
