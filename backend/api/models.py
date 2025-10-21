"""
api/models.py
Definición de modelos principales de la aplicación API de CropCare.

Puntos clave:
- Plot: valida que si el usuario pertenece a una empresa -> debe indicar Zona de su misma empresa.
         Si es individual -> NO puede indicar Zona.
         Notas de Plot: máx. 120 palabras.
- Inspection: la imagen es OBLIGATORIA AL CREAR (se valida en clean()).
              Notas de Inspection: máx. 240 palabras.
- Diagnostic: resultado de IA asociado a una Inspection.
- UserProfile: perfil "personal" (independiente de empresa).
- Report: registro del estado de generación de reportes.
"""

from django.conf import settings
from django.core.exceptions import ValidationError
from django.db import models

# Modelo de usuario configurado (User de Django)
User = settings.AUTH_USER_MODEL


class Cultivo(models.Model):
    """
    Modelo para representar un tipo de cultivo (ej: Trigo, Maíz, etc.).
    """
    nombre = models.CharField(max_length=100, unique=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Cultivo'
        verbose_name_plural = 'Cultivos'
        ordering = ['nombre']

    def __str__(self):
        return self.nombre


class Zona(models.Model):
    """
    Zona de cultivo perteneciente a una Empresa (organización).
    Agrupa múltiples parcelas (plots).
    """
    nombre = models.CharField(max_length=100)
    cultivo = models.ForeignKey(Cultivo, on_delete=models.PROTECT, related_name='zonas')
    empresa = models.ForeignKey('cropcare_orgs.Empresa', on_delete=models.PROTECT, related_name='zonas')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Zona'
        verbose_name_plural = 'Zonas'
        # Cada empresa no puede tener dos zonas con el mismo nombre
        unique_together = [('nombre', 'empresa')]

    def __str__(self):
        return f"{self.nombre} ({self.empresa.name})"


class Plot(models.Model):
    """
    Parcela (campo específico).
    - Para empresas: debe pertenecer a una Zona de la misma empresa.
    - Para usuarios individuales: NO debe tener Zona (es un plot personal).
    """
    name = models.CharField(max_length=100)
    zona = models.ForeignKey(Zona, null=True, blank=True, on_delete=models.PROTECT, related_name='plots')
    cultivo = models.ForeignKey(Cultivo, on_delete=models.PROTECT, related_name='plots')
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.PROTECT, related_name='plots')
    # Campo superficie_ha eliminado según especificaciones.
    notes = models.TextField(blank=True)  # máx. 120 palabras, se valida en clean()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Parcela'
        verbose_name_plural = 'Parcelas'
        ordering = ['-created_at']

    def __str__(self):
        return f"{self.name} - {self.cultivo}"

    def clean(self):
        """
        Validaciones de negocio:
        - Si el owner pertenece a una empresa -> zona obligatoria y de la misma empresa.
        - Si el owner es individual -> NO se permite zona.
        - Notas: máximo 120 palabras.
        """
        empresa = None
        profile = getattr(self.owner, 'profile', None)
        if profile:
            empresa = profile.empresa

        # Empresa: zona obligatoria y de la misma empresa
        if empresa and self.zona is None:
            raise ValidationError("Debes especificar una zona para la parcela si perteneces a una empresa.")
        if empresa and self.zona and self.zona.empresa_id != empresa.id:
            raise ValidationError("La zona especificada no pertenece a tu empresa.")

        # Individual: no se permite zona
        if not empresa and self.zona is not None:
            raise ValidationError("No puedes asignar una zona a una parcela si eres un usuario individual.")

        # Notas (máx. 120 palabras)
        if self.notes:
            num_words = len(self.notes.split())
            if num_words > 120:
                raise ValidationError("Las notas de la parcela no pueden exceder 120 palabras.")


class Inspection(models.Model):
    """
    Inspección realizada sobre una parcela (usualmente incluye una imagen para diagnóstico).
    """
    plot = models.ForeignKey(Plot, on_delete=models.CASCADE, related_name='inspections')
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.PROTECT, related_name='inspections')
    # Permitimos null=True para compatibilidad con filas antiguas,
    # pero la API y el admin obligan imagen en la creación.
    image = models.ImageField(upload_to='inspections/', null=True, blank=True)
    notes = models.TextField(blank=True)  # máx. 240 palabras (se valida en clean())
    inspected_at = models.DateTimeField(auto_now_add=True)  # timestamp del servidor

    class Meta:
        verbose_name = 'Inspección'
        verbose_name_plural = 'Inspecciones'
        ordering = ['-inspected_at']

    def __str__(self):
        return f"Inspección {self.id} - {self.plot}"

    def clean(self):
        """
        Validaciones:
        - La imagen es obligatoria al CREAR (cuando self.pk es None).
        - Notas: máximo 240 palabras.
        """
        # Imagen obligatoria al crear
        if self.pk is None and not self.image:
            raise ValidationError("La imagen es obligatoria para crear una inspección.")

        # Notas (máx. 240 palabras)
        if self.notes:
            num_words = len(self.notes.split())
            if num_words > 240:
                raise ValidationError("Las notas de la inspección no pueden exceder 240 palabras.")


class Diagnostic(models.Model):
    """
    Resultado de diagnóstico obtenido de una inspección (p. ej., detección de plaga o enfermedad).
    """
    inspection = models.ForeignKey(Inspection, on_delete=models.CASCADE, related_name='diagnostics')
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.PROTECT, related_name='diagnostics')
    label = models.CharField(max_length=100)       # Etiqueta o nombre del problema identificado
    confidence = models.FloatField()               # Nivel de confianza (0 a 1)
    model_version = models.CharField(max_length=20, default='1')  # Versión del modelo de IA utilizado
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Diagnóstico'
        verbose_name_plural = 'Diagnósticos'
        ordering = ['-created_at']

    def __str__(self):
        return f"{self.label} ({self.confidence * 100:.1f}%)"


class UserProfile(models.Model):
    """
    Perfil adicional del usuario para información de contacto u organización personal.
    Este perfil es independiente de la empresa a la que pueda pertenecer el usuario.
    """
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='userprofile')
    display_name = models.CharField(max_length=100, blank=True)
    organization = models.CharField(
        max_length=150,
        blank=True,  # organización personal (si no pertenece a Empresa formal)
    )
    phone = models.CharField(max_length=30, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Perfil de Usuario'
        verbose_name_plural = 'Perfiles de Usuario'

    def __str__(self):
        return f"Perfil de {self.user.username}"


class Report(models.Model):
    """
    Solicitud de reporte (p. ej., PDF) del historial de datos de un usuario o empresa.
    El reporte se genera asincrónicamente; este modelo registra su estado.
    """
    class ReportStatus(models.TextChoices):
        PENDING = 'PENDING', 'Pendiente'
        READY = 'READY', 'Listo'
        FAILED = 'FAILED', 'Fallido'

    class ReportFormat(models.TextChoices):
        PDF = 'PDF', 'PDF'
        CSV = 'CSV', 'CSV'

    title = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    format = models.CharField(max_length=10, choices=ReportFormat.choices, default=ReportFormat.PDF)
    status = models.CharField(max_length=10, choices=ReportStatus.choices, default=ReportStatus.PENDING)
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.PROTECT, related_name='reports')
    generated_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Reporte'
        verbose_name_plural = 'Reportes'
        ordering = ['-created_at']

    def __str__(self):
        return f"Reporte {self.id} - {self.title}"
