from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import SessionReport, AggregatedReport
from rest_framework.decorators import action
from rest_framework.response import Response
from django.db.models import Count, Avg, Q, F
from django.db.models.functions import TruncDate, TruncWeek, TruncMonth, TruncYear
from datetime import datetime, timedelta
from collections import defaultdict
from .serializers import SessionReportSerializer, AggregatedReportSerializer


class SessionReportViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet para obtener reportes de sesiones.
    Solo lectura (GET).
    """
    permission_classes = [IsAuthenticated]
    serializer_class = SessionReportSerializer

    def get_queryset(self):
        """Filtrar reportes por empresa del usuario"""
        user = self.request.user
        if hasattr(user, 'empresa'):
            return SessionReport.objects.filter(empresa=user.empresa).order_by('-generated_at')
        return SessionReport.objects.none()

    @action(detail=False, methods=['get'])
    def metrics(self, request):
        """
        Obtiene métricas agregadas de reportes.
        """
        user = request.user
        if not hasattr(user, 'empresa'):
            return Response({})

        # Filtros base
        reports = SessionReport.objects.filter(empresa=user.empresa)

        # Filtros opcionales
        period = request.query_params.get('period', 'month')
        date_from = request.query_params.get('date_from')
        date_to = request.query_params.get('date_to')
        cultivo_id = request.query_params.get('cultivo')
        zona_id = request.query_params.get('zona')

        if date_from:
            reports = reports.filter(generated_at__gte=date_from)
        if date_to:
            reports = reports.filter(generated_at__lte=date_to)
        if cultivo_id:
            reports = reports.filter(cultivo_id=cultivo_id)
        if zona_id:
            reports = reports.filter(zona_id=zona_id)

        # Contar usando suspicious_detections_count
        total_reports = 0
        reports_with_plagues = 0
        reports_healthy = 0
        all_labels = []
        total_confidence = 0

        for session in reports:
            # Total de detecciones
            session_detections = session.detections_count or 0
            total_reports += session_detections

            # Detecciones con plaga (del nuevo campo)
            session_plagues = session.suspicious_detections_count or 0
            reports_with_plagues += session_plagues

            # Sanas = Total - Plagas
            reports_healthy += (session_detections - session_plagues)

            # Acumular etiquetas
            if session.unique_labels:
                all_labels.extend(session.unique_labels)

            # Acumular confianza
            if session.average_confidence:
                total_confidence += session.average_confidence * session_detections

        avg_confidence = (total_confidence / total_reports) if total_reports > 0 else 0

        # Distribución de plagas vs sanas
        plague_distribution = {
            'healthy': reports_healthy,
            'with_plague': reports_with_plagues,
            'total': total_reports
        }

        # Top clasificaciones (enfermedades más comunes)
        label_counts = defaultdict(int)
        for label in all_labels:
            if 'Healthy' not in label and 'healthy' not in label.lower():
                label_counts[label] += 1

        top_diseases = [
            {'label': label, 'count': count}
            for label, count in sorted(label_counts.items(), key=lambda x: x[1], reverse=True)[:10]
        ]

        # Distribución por cultivo
        cultivo_stats = []
        for cultivo_group in reports.values('cultivo__nombre').annotate(session_count=Count('id')):
            cultivo_name = cultivo_group['cultivo__nombre']
            cultivo_sessions = reports.filter(cultivo__nombre=cultivo_name)

            cultivo_total = 0
            cultivo_plagues = 0

            for s in cultivo_sessions:
                s_detections = s.detections_count or 0
                s_plagues = s.suspicious_detections_count or 0

                cultivo_total += s_detections
                cultivo_plagues += s_plagues

            cultivo_healthy = cultivo_total - cultivo_plagues

            if cultivo_total > 0:
                cultivo_stats.append({
                    'cultivo__nombre': cultivo_name,
                    'total': cultivo_total,
                    'with_plague': cultivo_plagues,
                    'healthy': cultivo_healthy
                })

        cultivo_stats.sort(key=lambda x: x['total'], reverse=True)

        # Distribución por zona
        zona_stats = []
        for zona_group in reports.values('zona__nombre').annotate(session_count=Count('id')):
            zona_name = zona_group['zona__nombre']
            zona_sessions = reports.filter(zona__nombre=zona_name)

            zona_total = 0
            zona_plagues = 0

            for s in zona_sessions:
                s_detections = s.detections_count or 0
                s_plagues = s.suspicious_detections_count or 0

                zona_total += s_detections
                zona_plagues += s_plagues

            zona_healthy = zona_total - zona_plagues

            if zona_total > 0:
                zona_stats.append({
                    'zona__nombre': zona_name,
                    'total': zona_total,
                    'with_plague': zona_plagues,
                    'healthy': zona_healthy
                })

        zona_stats.sort(key=lambda x: x['total'], reverse=True)

        # Tendencia temporal
        trunc_func = {
            'day': TruncDate,
            'week': TruncWeek,
            'month': TruncMonth,
            'year': TruncYear
        }.get(period, TruncMonth)

        timeline_data = reports.annotate(
            period=trunc_func('generated_at')
        ).values('period').annotate(
            session_count=Count('id')
        ).order_by('period')

        timeline = []
        for item in timeline_data:
            period_sessions = reports.annotate(
                period_trunc=trunc_func('generated_at')
            ).filter(period_trunc=item['period'])

            period_total = 0
            period_plagues = 0
            period_confidence = 0

            for s in period_sessions:
                s_detections = s.detections_count or 0
                s_plagues = s.suspicious_detections_count or 0

                period_total += s_detections
                period_plagues += s_plagues

                if s.average_confidence:
                    period_confidence += s.average_confidence * s_detections

            period_healthy = period_total - period_plagues
            period_avg_confidence = (period_confidence / period_total) if period_total > 0 else 0

            timeline.append({
                'date': item['period'].isoformat() if item['period'] else None,
                'total': period_total,
                'with_plague': period_plagues,
                'healthy': period_healthy,
                'avg_confidence': round(period_avg_confidence, 2)
            })

        return Response({
            'summary': {
                'total_reports': total_reports,
                'reports_with_plagues': reports_with_plagues,
                'reports_healthy': reports_healthy,
                'avg_confidence': round(avg_confidence, 2),
                'plague_percentage': round((reports_with_plagues / total_reports * 100) if total_reports > 0 else 0, 1)
            },
            'plague_distribution': plague_distribution,
            'top_diseases': top_diseases,
            'by_cultivo': cultivo_stats,
            'by_zona': zona_stats,
            'timeline': timeline
        })

    @action(detail=False, methods=['get'])
    def export_pdf(self, request):
        """
        Genera PDF con métricas.

        GET /api/reports/session-reports/export_pdf/
        """
        from django.http import HttpResponse
        from reportlab.lib.pagesizes import A4
        from reportlab.lib import colors
        from reportlab.lib.units import inch
        from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
        from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
        from reportlab.lib.enums import TA_CENTER
        from io import BytesIO

        user = request.user
        if not hasattr(user, 'empresa'):
            return Response({'error': 'Usuario sin empresa'}, status=400)

        # Obtener métricas (reutilizar lógica)
        metrics_view = self.metrics(request)
        metrics = metrics_view.data

        # Crear PDF
        buffer = BytesIO()
        doc = SimpleDocTemplate(buffer, pagesize=A4, topMargin=0.5 * inch, bottomMargin=0.5 * inch)
        story = []
        styles = getSampleStyleSheet()

        # Estilo personalizado
        title_style = ParagraphStyle(
            'CustomTitle',
            parent=styles['Heading1'],
            fontSize=24,
            textColor=colors.HexColor('#1976D2'),
            spaceAfter=30,
            alignment=TA_CENTER
        )

        heading_style = ParagraphStyle(
            'CustomHeading',
            parent=styles['Heading2'],
            fontSize=16,
            textColor=colors.HexColor('#1976D2'),
            spaceAfter=12,
            spaceBefore=12
        )

        # Título
        story.append(Paragraph(f"Reporte de Métricas - {user.empresa.name}", title_style))
        story.append(Paragraph(f"Generado: {datetime.now().strftime('%d/%m/%Y %H:%M')}", styles['Normal']))
        story.append(Spacer(1, 0.3 * inch))

        # Resumen
        story.append(Paragraph("Resumen General", heading_style))
        summary_data = [
            ['Métrica', 'Valor'],
            ['Total de Reportes', str(metrics['summary']['total_reports'])],
            ['Reportes con Plagas', str(metrics['summary']['reports_with_plagues'])],
            ['Reportes Saludables', str(metrics['summary']['reports_healthy'])],
            ['% de Plagas', f"{metrics['summary']['plague_percentage']}%"],
            ['Confianza Promedio', f"{metrics['summary']['avg_confidence'] * 100:.1f}%"],
        ]

        summary_table = Table(summary_data, colWidths=[3 * inch, 2 * inch])
        summary_table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1976D2')),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, 0), 12),
            ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
            ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
            ('GRID', (0, 0), (-1, -1), 1, colors.black),
        ]))
        story.append(summary_table)
        story.append(Spacer(1, 0.3 * inch))

        # Top enfermedades
        if metrics['top_diseases']:
            story.append(Paragraph("Top 10 Enfermedades Detectadas", heading_style))
            disease_data = [['Enfermedad', 'Cantidad']]
            for disease in metrics['top_diseases'][:10]:
                disease_data.append([disease['label'].replace('_', ' '), str(disease['count'])])

            disease_table = Table(disease_data, colWidths=[4 * inch, 1.5 * inch])
            disease_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#F44336')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                ('FONTSIZE', (0, 0), (-1, 0), 12),
                ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                ('BACKGROUND', (0, 1), (-1, -1), colors.lightgrey),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
            ]))
            story.append(disease_table)
            story.append(Spacer(1, 0.3 * inch))

        # Por cultivo
        if metrics['by_cultivo']:
            story.append(Paragraph("Distribución por Cultivo", heading_style))
            cultivo_data = [['Cultivo', 'Total', 'Con Plagas', 'Saludables']]
            for cultivo in metrics['by_cultivo']:
                cultivo_data.append([
                    cultivo['cultivo__nombre'],
                    str(cultivo['total']),
                    str(cultivo['with_plague']),
                    str(cultivo['healthy'])
                ])

            cultivo_table = Table(cultivo_data, colWidths=[2.5 * inch, 1 * inch, 1.5 * inch, 1.5 * inch])
            cultivo_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#4CAF50')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                ('FONTSIZE', (0, 0), (-1, 0), 12),
                ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                ('BACKGROUND', (0, 1), (-1, -1), colors.lightgreen),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
            ]))
            story.append(cultivo_table)

        # Construir PDF
        doc.build(story)

        # Respuesta
        pdf = buffer.getvalue()
        buffer.close()

        response = HttpResponse(pdf, content_type='application/pdf')
        response[
            'Content-Disposition'] = f'attachment; filename="metricas_{datetime.now().strftime("%Y%m%d_%H%M%S")}.pdf"'
        return response


class AggregatedReportViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet para reportes agregados (dashboards).
    """
    permission_classes = [IsAuthenticated]
    serializer_class = AggregatedReportSerializer

    def get_queryset(self):
        """Filtrar por empresa del usuario"""
        user = self.request.user
        if hasattr(user, 'empresa'):
            return AggregatedReport.objects.filter(empresa=user.empresa).order_by('-date')
        return AggregatedReport.objects.none()