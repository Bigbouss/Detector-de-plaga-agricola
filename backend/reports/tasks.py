from celery import shared_task
from django.db.models import Avg, Count
from scanners.models import ScannerSession, ScannerResult
from .models import SessionReport
import statistics


@shared_task
def generate_session_report_async(session_id):
    """
    Genera un reporte de sesión de forma asíncrona.

    Args:
        session_id: UUID de la sesión (session_id, no id)
    """
    try:
        # Usar session_id como primary key
        session = ScannerSession.objects.get(session_id=session_id)
        images = session.images.all()

        # Filtrar por session directamente
        results = ScannerResult.objects.filter(session=session)

        detections_count = results.count()

        # Usar 'classification'
        labels = list(results.values_list('classification', flat=True))
        confidences = [r.confidence for r in results]

        # Contar detecciones con plaga
        plague_labels = [label for label in labels if 'Healthy' not in label]
        suspicious_detections_count = len(plague_labels)

        top_labels = (
            results.values('classification')
            .annotate(count=Count('classification'))
            .order_by('-count')[:5]
        )

        avg_conf = statistics.mean(confidences) if confidences else 0.0
        med_conf = statistics.median(confidences) if confidences else 0.0

        lat_avg = images.aggregate(avg_lat=Avg('latitude'))['avg_lat']
        lon_avg = images.aggregate(avg_lon=Avg('longitude'))['avg_lon']

        SessionReport.objects.create(
            session=session,
            empresa=session.empresa,
            zona=session.zona,
            cultivo=session.cultivo,
            owner=session.owner,
            images_count=images.count(),
            detections_count=detections_count,
            suspicious_detections_count=suspicious_detections_count,
            unique_labels=list(set(labels)),
            top_labels=list(top_labels),
            average_confidence=avg_conf,
            median_confidence=med_conf,
            lat_avg=lat_avg,
            lon_avg=lon_avg,
            low_confidence_flag=(avg_conf < 0.6),
            suspicious_flag=(suspicious_detections_count > 0),
        )

        print(f"SessionReport generado para sesión {session_id}")
        print(
            f" Total: {detections_count}, Plagas: {suspicious_detections_count}, Sanas: {detections_count - suspicious_detections_count}")

    except ScannerSession.DoesNotExist:
        print(f"Sesión {session_id} no encontrada")
    except Exception as e:
        print(f"Error generando SessionReport para session {session_id}: {e}")