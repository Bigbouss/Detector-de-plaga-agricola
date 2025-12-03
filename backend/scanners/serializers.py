from rest_framework import serializers
from .models import ModelVersion, ScannerSession, ScannerImage, ScannerResult


class ModelVersionSerializer(serializers.ModelSerializer):
    class Meta:
        model = ModelVersion
        fields = ['id', 'name', 'version', 'framework', 'file_path', 'created_at']

class ScannerResultSerializer(serializers.ModelSerializer):
    image_url = serializers.SerializerMethodField()
    
    class Meta:
        model = ScannerResult
        fields = [
            'result_id', 'session', 'photo_path', 'classification', 
            'confidence', 'has_plague', 'report_id', 'scanned_at', 
            'created_at', 'bbox', 'image_url'
        ]
        read_only_fields = ['created_at', 'image_url']
    
    def get_image_url(self, obj):
        """Genera URL para obtener la imagen"""
        request = self.context.get('request')
        if obj.photo_path and request:
            return request.build_absolute_uri(
                f'/api/scanners/results/{obj.result_id}/image/'
            )
        return None

class ScannerImageSerializer(serializers.ModelSerializer):
    results = ScannerResultSerializer(many=True, read_only=True)

    class Meta:
        model = ScannerImage
        fields = [
            'id', 'session', 'image', 'local_photo_path',
            'taken_at', 'latitude', 'longitude', 'created_at', 'results'
        ]
        read_only_fields = ['id', 'created_at']

    def validate_image(self, image):
        """Valida que no sea demasiado grande ni corrupta."""
        from .models import validate_image_format
        validate_image_format(image)
        return image


class ScannerSessionSerializer(serializers.ModelSerializer):
    images = ScannerImageSerializer(many=True, read_only=True)
    scan_results = ScannerResultSerializer(many=True, read_only=True)
    
    # Nombres legibles
    empresa_name = serializers.CharField(source='empresa.name', read_only=True)
    zona_name = serializers.CharField(source='zona.nombre', read_only=True)
    cultivo_name = serializers.CharField(source='cultivo.nombre', read_only=True)

    class Meta:
        model = ScannerSession
        fields = [
            'session_id', 'empresa', 'empresa_name', 'zona', 'zona_name',
            'cultivo', 'cultivo_name', 'owner', 'worker_name',
            'model_version', 'model_version_string',
            'started_at', 'finished_at', 'status',
            'total_scans', 'healthy_count', 'plague_count',
            'notes', 'created_at', 'updated_at',
            'images', 'scan_results'
        ]
        read_only_fields = ['created_at', 'updated_at', 'images', 'scan_results']

    def validate(self, attrs):
        empresa = attrs.get('empresa')
        cultivo = attrs.get('cultivo')
        if cultivo and empresa and cultivo.zona.empresa != empresa:
            raise serializers.ValidationError("El cultivo no pertenece a la empresa seleccionada.")
        return attrs

class ScannerSessionCreateSerializer(serializers.ModelSerializer):
    """Serializer simplificado para crear sesiones desde el cliente."""
    
    class Meta:
        model = ScannerSession
        fields = [
            'session_id', 'empresa', 'zona', 'cultivo', 'worker_name',
            'model_version_string', 'started_at', 'status'
        ]
    
    def create(self, validated_data):
        validated_data['owner'] = self.context['request'].user
        return super().create(validated_data)


class ScanResultCreateSerializer(serializers.ModelSerializer):
    """Serializer para crear resultados de escaneo desde el cliente."""
    
    class Meta:
        model = ScannerResult
        fields = [
            'result_id', 'session', 'photo_path', 'classification',
            'confidence', 'has_plague', 'report_id', 'scanned_at'
        ]
    
    def validate_session(self, value):
        """Valida que la sesión exista y esté activa."""
        if value.status != 'ACTIVE':
            raise serializers.ValidationError("No se pueden agregar escaneos a una sesión no activa.")
        return value

class ScannerSessionWithReportSerializer(serializers.ModelSerializer):
    """Sesión con reporte Y resultados anidados"""
    from reports.serializers import SessionReportSerializer
    
    report = SessionReportSerializer(source='session_report', read_only=True)
    scan_results = ScannerResultSerializer(many=True, read_only=True)
    empresa_name = serializers.CharField(source='empresa.name', read_only=True)
    zona_name = serializers.CharField(source='zona.nombre', read_only=True)
    cultivo_name = serializers.CharField(source='cultivo.nombre', read_only=True)
    
    class Meta:
        model = ScannerSession
        fields = [
            'session_id', 'empresa', 'empresa_name', 'zona', 'zona_name',
            'cultivo', 'cultivo_name', 'owner', 'worker_name',
            'started_at', 'finished_at', 'status',
            'total_scans', 'healthy_count', 'plague_count',
            'notes', 'report', 'scan_results'
        ]