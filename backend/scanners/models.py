from django.db import models, transaction
from django.conf import settings
from django.utils import timezone
from django.core.exceptions import ValidationError
from PIL import Image

User = settings.AUTH_USER_MODEL


# ------------------- VALIDADORES -------------------
def validate_image_format(image):
    """Valida que el formato sea válido y no corrupto."""
    try:
        img = Image.open(image)
        img.verify()
        if img.format not in ["JPEG", "PNG"]:
            raise ValidationError("Solo se permiten imágenes JPEG o PNG.")
        if image.size > 5 * 1024 * 1024:  # 5MB máximo
            raise ValidationError("La imagen excede los 5MB permitidos.")
    except Exception:
        raise ValidationError("Imagen inválida o corrupta.")


# ------------------- MODELOS -------------------
class ModelVersion(models.Model):
    """Registro del modelo ML usado."""
    name = models.CharField(max_length=100)
    version = models.CharField(max_length=50)
    file_path = models.CharField(max_length=512, blank=True, null=True)
    framework = models.CharField(max_length=50, default='tflite')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('name', 'version')
        verbose_name = 'Model Version'
        verbose_name_plural = 'Model Versions'

    def __str__(self):
        return f"{self.name} v{self.version} ({self.framework})"


class ScannerSession(models.Model):
    """Una sesión de escaneo iniciada por un worker en un cultivo."""
    
    STATUS_CHOICES = [
        ('ACTIVE', 'Activa'),
        ('COMPLETED', 'Completada'),
        ('CANCELLED', 'Cancelada'),
    ]
    
    # ID único desde el cliente (UUID)
    session_id = models.CharField(max_length=36, unique=True, primary_key=True)
    
    empresa = models.ForeignKey('emprises.Empresa', on_delete=models.CASCADE, related_name='scanner_sessions')
    zona = models.ForeignKey('zonecrop.Zona', on_delete=models.CASCADE, related_name='scanner_sessions')
    cultivo = models.ForeignKey('zonecrop.Cultivo', on_delete=models.CASCADE, related_name='scanner_sessions')
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='scanner_sessions')
    
    # Información del worker
    worker_name = models.CharField(max_length=200)
    
    model_version = models.ForeignKey(ModelVersion, on_delete=models.SET_NULL, null=True, blank=True)
    model_version_string = models.CharField(max_length=50, default='1.0')  # versión como string
    
    # Timestamps
    started_at = models.DateTimeField(default=timezone.now)
    finished_at = models.DateTimeField(null=True, blank=True)
    
    # Estado
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='ACTIVE')
    
    # Métricas
    total_scans = models.IntegerField(default=0)
    healthy_count = models.IntegerField(default=0)
    plague_count = models.IntegerField(default=0)
    
    notes = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        indexes = [
            models.Index(fields=['empresa', 'zona', 'cultivo']),
            models.Index(fields=['owner', 'status']),
            models.Index(fields=['started_at']),
        ]
        verbose_name = 'Scanner Session'
        verbose_name_plural = 'Scanner Sessions'
        ordering = ['-started_at']

    def clean(self):
        if self.empresa != self.cultivo.zona.empresa:
            raise ValidationError("La empresa no coincide con la del cultivo.")
        if self.started_at and self.finished_at and self.finished_at < self.started_at:
            raise ValidationError("La fecha de finalización no puede ser anterior al inicio.")


class ScannerImage(models.Model):
    """Imagen tomada durante la sesión."""
    session = models.ForeignKey(ScannerSession, on_delete=models.CASCADE, related_name='images')
    image = models.ImageField(upload_to='scans/%Y/%m/%d/', validators=[validate_image_format])
    
    # Path local del cliente (para referencia)
    local_photo_path = models.CharField(max_length=500, blank=True, null=True)
    
    taken_at = models.DateTimeField(default=timezone.now)
    latitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    longitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['id']
        verbose_name = 'Scanner Image'
        verbose_name_plural = 'Scanner Images'

    def __str__(self):
        return f"Image {self.id} - Session {self.session.session_id}"


class ScannerResult(models.Model):
    """Resultado de inferencia de una imagen."""
    
    # ID único desde el cliente
    result_id = models.CharField(max_length=36, unique=True, primary_key=True)
    
    session = models.ForeignKey(ScannerSession, on_delete=models.CASCADE, related_name='scan_results')
    image = models.ForeignKey(ScannerImage, on_delete=models.CASCADE, related_name='results', null=True, blank=True)
    
    # Path local de la foto (sin subir imagen completa si no es necesario)
    photo_path = models.CharField(max_length=500)
    
    # Clasificación
    classification = models.CharField(max_length=200)  # ej: "healthy" o "Potato___Late_blight"
    confidence = models.FloatField()  # 0.0 - 1.0
    has_plague = models.BooleanField(default=False)
    
    # Vinculación con reporte (si se creó uno)
    report_id = models.IntegerField(null=True, blank=True)
    
    scanned_at = models.DateTimeField(default=timezone.now)
    created_at = models.DateTimeField(auto_now_add=True)
    
    bbox = models.JSONField(null=True, blank=True)  # opcional: {x, y, w, h}

    class Meta:
        indexes = [
            models.Index(fields=['session', 'has_plague']),
            models.Index(fields=['classification']),
        ]
        verbose_name = 'Scanner Result'
        verbose_name_plural = 'Scanner Results'
        ordering = ['-scanned_at']

    def __str__(self):
        return f"{self.classification} ({self.confidence:.2f})"