from django.contrib import admin
from .models import SessionReport, AggregatedReport


@admin.register(SessionReport)
class SessionReportAdmin(admin.ModelAdmin):
    list_display = [
        'id', 'session', 'empresa', 'zona', 'cultivo', 'owner',
        'detections_count', 'suspicious_flag', 'generated_at'
    ]
    list_filter = ['empresa', 'suspicious_flag', 'low_confidence_flag', 'generated_at']
    search_fields = ['session__session_id', 'owner__email']
    readonly_fields = ['created_at', 'generated_at']
    
    fieldsets = (
        ('Información de Sesión', {
            'fields': ('session', 'empresa', 'zona', 'cultivo', 'owner')
        }),
        ('Métricas', {
            'fields': (
                'images_count', 'detections_count', 'unique_labels', 'top_labels',
                'average_confidence', 'median_confidence'
            )
        }),
        ('Geolocalización', {
            'fields': ('lat_avg', 'lon_avg')
        }),
        ('Banderas', {
            'fields': ('low_confidence_flag', 'suspicious_flag')
        }),
        ('Timestamps', {
            'fields': ('created_at', 'generated_at')
        }),
    )


@admin.register(AggregatedReport)
class AggregatedReportAdmin(admin.ModelAdmin):
    list_display = [
        'id', 'empresa', 'zona', 'cultivo', 'date', 'granularity',
        'total_sessions', 'total_detections'
    ]
    list_filter = ['granularity', 'empresa', 'date']
    search_fields = ['empresa__name']