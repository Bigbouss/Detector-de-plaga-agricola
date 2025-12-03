from django.db import models
from django.core.exceptions import ValidationError
from django.utils import timezone
from django.conf import settings

User = settings.AUTH_USER_MODEL  # string (accounts.User)

class TimeStampedModel(models.Model):
    created_at = models.DateTimeField(auto_now_add=True, null=False)
    updated_at = models.DateTimeField(auto_now=True, null=False)

    class Meta:
        abstract = True


class Zona(TimeStampedModel):
    nombre = models.CharField(max_length=255)
    descripcion = models.TextField(blank=True, null=True)
    empresa = models.ForeignKey(
        'emprises.Empresa',
        on_delete=models.CASCADE,
        related_name='zonas'
    )

    class Meta:
        db_table = 'zonecrop_zonas'
        ordering = ['-created_at']
        unique_together = [['nombre', 'empresa']]
        indexes = [
            models.Index(fields=['empresa', 'nombre']),
        ]

    def __str__(self):
        return f"{self.nombre} - {self.empresa.name}"

    def clean(self):
        # Normalizar nombre simple
        if self.nombre:
            self.nombre = self.nombre.strip()
        # Comprueba empresa existente (integridad referencial ya lo hace)
        if not self.empresa_id:
            raise ValidationError("Una Zona debe pertenecer a una Empresa.")


class Cultivo(TimeStampedModel):
    nombre = models.CharField(max_length=100)
    zona = models.ForeignKey(
        Zona,
        on_delete=models.CASCADE,
        related_name='cultivos'
    )

    class Meta:
        db_table = 'zonecrop_cultivos'
        ordering = ['nombre']
        unique_together = [['nombre', 'zona']]
        indexes = [
            models.Index(fields=['zona', 'nombre']),
        ]

    def __str__(self):
        return f"{self.nombre} ({self.zona.nombre})"

    def clean(self):
        if self.nombre:
            self.nombre = self.nombre.strip()
        if not self.zona_id:
            raise ValidationError("El Cultivo debe pertenecer a una Zona.")


class WorkerZoneAssignment(models.Model):
    """
    Asignación de trabajadores a Zonas (permiso para acceder a la zona).
    Si quieres asignar por cultivo, usa WorkerCultivoAssignment.
    """
    worker = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='zone_assignments'
    )
    zona = models.ForeignKey(
        Zona,
        on_delete=models.CASCADE,
        related_name='worker_assignments'
    )
    assigned_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'zonecrop_worker_zone_assignments'
        unique_together = [['worker', 'zona']]
        verbose_name = 'Asignación trabajador-zona'
        verbose_name_plural = 'Asignaciones trabajador-zona'
        indexes = [
            models.Index(fields=['zona', 'worker']),
        ]

    def __str__(self):
        return f"{self.worker.email} -> {self.zona.nombre}"

    def clean(self):
        # regla: worker debe pertenecer a la misma empresa que la zona
        # (esto evita asignar un worker de otra empresa)
        worker_empresa_id = getattr(self.worker, 'empresa_id', None)
        if worker_empresa_id is None:
            raise ValidationError("El worker no está asociado a ninguna empresa.")
        if worker_empresa_id != self.zona.empresa_id:
            raise ValidationError("No puedes asignar un worker a una zona de otra empresa.")
