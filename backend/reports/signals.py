from django.db.models.signals import post_save
from django.dispatch import receiver
from scanners.models import ScannerSession


@receiver(post_save, sender=ScannerSession)
def on_session_finished(sender, instance, created, **kwargs):
    """
    Genera un reporte automáticamente cuando la sesión se marca como COMPLETED.
    """
    # Solo generar si la sesión está completada
    if instance.status == 'COMPLETED' and instance.finished_at:
        from .models import SessionReport
        from django.db.models import Avg, Count
        import statistics

        # Evitar crear reportes duplicados
        if SessionReport.objects.filter(session=instance).exists():
            print(f"Reporte ya existe para sesión {instance.session_id}")
            return

        try:
            # Obtener datos de la sesión
            images = instance.images.all()
            results = instance.scan_results.all()

            detections_count = results.count()

            # Obtener todas las clasificaciones
            labels = list(results.values_list('classification', flat=True))
            confidences = [r.confidence for r in results]

            # detecciones con plaga vs sanas
            plague_labels = [label for label in labels if 'Healthy' not in label]
            suspicious_detections_count = len(plague_labels)
            healthy_detections_count = detections_count - suspicious_detections_count

            # Top 5 clasificaciones más comunes
            top_labels = (
                results.values('classification')
                .annotate(count=Count('classification'))
                .order_by('-count')[:5]
            )

            # Estadísticas de confianza
            avg_conf = statistics.mean(confidences) if confidences else 0.0
            med_conf = statistics.median(confidences) if confidences else 0.0

            # Promedios de ubicación
            lat_avg = images.aggregate(avg_lat=Avg('latitude'))['avg_lat']
            lon_avg = images.aggregate(avg_lon=Avg('longitude'))['avg_lon']

            # Flag de baja confianza (si el promedio es menor a 60%)
            low_confidence = avg_conf < 0.6

            # Crear reporte
            report = SessionReport.objects.create(
                session=instance,
                empresa=instance.empresa,
                zona=instance.zona,
                cultivo=instance.cultivo,
                owner=instance.owner,
                images_count=images.count(),
                detections_count=detections_count,
                suspicious_detections_count=suspicious_detections_count,
                unique_labels=list(set(labels)),
                top_labels=list(top_labels),
                average_confidence=avg_conf,
                median_confidence=med_conf,
                lat_avg=lat_avg,
                lon_avg=lon_avg,
                low_confidence_flag=low_confidence,
                suspicious_flag=(suspicious_detections_count > 0),
            )

            print(f"SessionReport {report.id} generado para sesión {instance.session_id}")
            print(
                f"   Detecciones: {detections_count} | Plagas: {suspicious_detections_count} | Sanas: {healthy_detections_count} | Confianza: {avg_conf:.2%}")

        except Exception as e:
            print(f"Error generando SessionReport para sesión {instance.session_id}: {e}")
            import traceback
            traceback.print_exc()