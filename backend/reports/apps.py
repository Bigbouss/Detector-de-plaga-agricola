from django.apps import AppConfig

class ReportsConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'reports'
    label = 'reports'
    verbose_name = 'Módulo de Reportes y Métricas'

    def ready(self):
        import reports.signals  # noqa
