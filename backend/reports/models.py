from django.db import models
from django.conf import settings
from django.utils import timezone

User = settings.AUTH_USER_MODEL


class SessionReport(models.Model):
    """
    Resumen generado cuando una ScannerSession finaliza.
    Un registro por ScannerSession (1:1).
    """
    session = models.OneToOneField(
        'scanners.ScannerSession',
        on_delete=models.CASCADE,
        related_name='session_report'
    )
    empresa = models.ForeignKey('emprises.Empresa', on_delete=models.CASCADE, related_name='session_reports')
    zona = models.ForeignKey('zonecrop.Zona', on_delete=models.CASCADE, related_name='session_reports')
    cultivo = models.ForeignKey('zonecrop.Cultivo', on_delete=models.CASCADE, related_name='session_reports')
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='session_reports')

    # métricas primarias
    images_count = models.PositiveIntegerField(default=0)
    detections_count = models.PositiveIntegerField(default=0)
    suspicious_detections_count = models.PositiveIntegerField(default=0)
    unique_labels = models.JSONField(default=list)
    top_labels = models.JSONField(default=list)
    average_confidence = models.FloatField(null=True, blank=True)
    median_confidence = models.FloatField(null=True, blank=True)

    # geolocalización promedio
    lat_avg = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    lon_avg = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)

    # banderas
    low_confidence_flag = models.BooleanField(default=False)
    suspicious_flag = models.BooleanField(default=False)

    notes = models.TextField(blank=True, null=True)

    created_at = models.DateTimeField(auto_now_add=True)
    generated_at = models.DateTimeField(default=timezone.now)

    class Meta:
        indexes = [
            models.Index(fields=['empresa', 'zona', 'cultivo']),
            models.Index(fields=['generated_at']),
        ]
        ordering = ['-generated_at']

    def __str__(self):
        return f"SessionReport(session={self.session_id}, empresa={self.empresa.name})"


class AggregatedReport(models.Model):
    """
    Datos agregados para dashboards (ETL / KPI).
    """
    GRANULARITY_CHOICES = [
        ('daily', 'Daily'),
        ('monthly', 'Monthly'),
    ]

    empresa = models.ForeignKey('emprises.Empresa', on_delete=models.CASCADE, related_name='aggregated_reports')
    zona = models.ForeignKey('zonecrop.Zona', on_delete=models.CASCADE, related_name='aggregated_reports', null=True, blank=True)
    cultivo = models.ForeignKey('zonecrop.Cultivo', on_delete=models.CASCADE, related_name='aggregated_reports', null=True, blank=True)

    date = models.DateField()
    granularity = models.CharField(max_length=10, choices=GRANULARITY_CHOICES, default='daily')

    total_sessions = models.PositiveIntegerField(default=0)
    total_images = models.PositiveIntegerField(default=0)
    total_detections = models.PositiveIntegerField(default=0)
    avg_confidence = models.FloatField(null=True, blank=True)
    top_labels = models.JSONField(default=list)
    prevalence = models.JSONField(default=dict)

    created_at = models.DateTimeField(auto_now_add=True)
    last_refreshed_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        unique_together = ('empresa', 'zona', 'cultivo', 'date', 'granularity')
        indexes = [models.Index(fields=['empresa', 'date', 'granularity'])]

    def __str__(self):
        return f"AggregatedReport({self.empresa.name}, {self.date}, {self.granularity})"
