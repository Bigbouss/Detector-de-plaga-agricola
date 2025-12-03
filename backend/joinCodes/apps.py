from django.apps import AppConfig

class JoincodesConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "joinCodes"    # nombre del paquete (módulo).
    label = "joincodes"   # label corto usado por Django
    verbose_name = "Gestión de Códigos de trabajadores"
