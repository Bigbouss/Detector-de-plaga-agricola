from rest_framework import serializers
from .models import SessionReport, AggregatedReport


class SessionReportSerializer(serializers.ModelSerializer):
    class Meta:
        model = SessionReport
        fields = [
            'id', 'session', 'empresa', 'zona', 'cultivo', 'owner',
            'images_count', 'detections_count', 'unique_labels', 'top_labels',
            'average_confidence', 'median_confidence',
            'lat_avg', 'lon_avg',
            'low_confidence_flag', 'suspicious_flag',
            'notes', 'created_at', 'generated_at'
        ]
        read_only_fields = ['id', 'created_at', 'generated_at']


class AggregatedReportSerializer(serializers.ModelSerializer):
    class Meta:
        model = AggregatedReport
        fields = '__all__'