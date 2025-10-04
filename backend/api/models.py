# api/models.py
from __future__ import annotations

import uuid
from decimal import Decimal

from django.conf import settings
from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator


class TimeStampedModel(models.Model):
    """
    Base con timestamps; la heredamos en todas las entidades.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    created_at = models.DateTimeField(auto_now_add=True)  # cuándo se creó
    updated_at = models.DateTimeField(auto_now=True)      # última modificación

    class Meta:
        abstract = True


class Plot(TimeStampedModel):
    """
    Parcela/Lote que será gestionada por cada usuario.
    Reglas principales:
      - 'owner' es el usuario dueño.
      - 'name' debe ser único por usuario (owner + name).
      - 'superficie_ha' debe ser > 0.
    """
    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="plots"
    )
    name = models.CharField(max_length=100)
    cultivo = models.CharField(max_length=50)
    superficie_ha = models.DecimalField(
        max_digits=8,
        decimal_places=2,
        validators=[MinValueValidator(Decimal("0.01"))],  # > 0
    )
    fecha_siembra = models.DateField()
    notes = models.TextField(blank=True, default="")

    class Meta:
        ordering = ["-created_at"]
        # Evita nombres duplicados para el mismo usuario
        constraints = [
            models.UniqueConstraint(
                fields=["owner", "name"],
                name="uniq_plot_owner_name",
            ),
        ]

    def __str__(self) -> str:
        return f"{self.name} ({self.cultivo})"


class Inspection(TimeStampedModel):
    """
    Inspección de una parcela:
      - Guardamos 'owner' para aplicar el corte por dueño de manera uniforme.
      - 'plot' es FK a Plot (en cascada).
    """
    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="inspections"
    )
    plot = models.ForeignKey(Plot, on_delete=models.CASCADE, related_name="inspections")
    inspected_at = models.DateTimeField(auto_now_add=True)
    notes = models.TextField(blank=True, default="")
    # Si vas a subir imagen, instala Pillow (pip install Pillow)
    photo = models.ImageField(upload_to="inspections/photos/", blank=True, null=True)

    class Meta:
        ordering = ["-inspected_at"]

    def __str__(self) -> str:
        return f"Inspection {self.id} on {self.plot.name}"


class Diagnostic(TimeStampedModel):
    """
    Resultado de un modelo ML para una inspección.
      - 'confidence' limitado a [0, 1].
      - 'model_version' y 'device_id' son metadatos útiles.
    """
    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="diagnostics"
    )
    inspection = models.ForeignKey(
        Inspection, on_delete=models.CASCADE, related_name="diagnostics"
    )
    label = models.CharField(max_length=100)
    confidence = models.DecimalField(
        max_digits=4,
        decimal_places=2,
        validators=[MinValueValidator(Decimal("0.00")), MaxValueValidator(Decimal("1.00"))],
    )
    model_version = models.CharField(max_length=50, default="tflite-v1")
    device_id = models.CharField(max_length=100, blank=True, default="")

    class Meta:
        ordering = ["-created_at"]

    def __str__(self) -> str:
        return f"{self.label} ({self.confidence})"


class Report(TimeStampedModel):
    """
    Historial de reportes generados (por ejemplo PDF/CSV/JSON):
      - Se crea en 'pending' y luego un proceso externo puede generar el archivo,
        setear 'file' y cambiar 'status' a 'ready'.
    """
    FORMAT_CHOICES = (
        ("pdf", "PDF"),
        ("csv", "CSV"),
        ("json", "JSON"),
    )
    STATUS_CHOICES = (
        ("pending", "Pending"),
        ("ready", "Ready"),
        ("failed", "Failed"),
    )

    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="reports"
    )
    title = models.CharField(max_length=120)
    description = models.TextField(blank=True, default="")

    format = models.CharField(max_length=10, choices=FORMAT_CHOICES, default="pdf")
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, default="pending")
    generated_at = models.DateTimeField(blank=True, null=True)

    # Si algún día guardas el archivo del reporte:
    file = models.FileField(upload_to="reports/", blank=True, null=True)

    class Meta:
        ordering = ["-created_at"]

    def __str__(self) -> str:
        return f"[{self.status}] {self.title}"


class UserProfile(TimeStampedModel):
    """
    Perfil extendido del usuario (datos no sensibles para la UI).
    Se crea lazy (on-demand) al primer GET del endpoint /auth/profile/.
    """
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="profile"
    )
    display_name = models.CharField(max_length=120, blank=True, default="")
    organization = models.CharField(max_length=120, blank=True, default="")
    phone = models.CharField(max_length=30, blank=True, default="")

    def __str__(self) -> str:
        return f"Profile of {self.user.username}"
