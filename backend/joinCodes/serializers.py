from rest_framework import serializers
from django.utils import timezone
from django.apps import apps
from .models import JoinCode, JoinCodeUsage

class JoinCodeSerializer(serializers.ModelSerializer):
    empresa_name = serializers.CharField(source='empresa.name', read_only=True)
    # permitir que admin pase un código manual opcional (solo admin de empresa)
    manual_code = serializers.CharField(write_only=True, required=False, allow_blank=False)

    class Meta:
        model = JoinCode
        fields = [
            'id', 'code', 'manual_code', 'empresa', 'empresa_name', 'role',
            'max_uses', 'used_count', 'expires_at', 'revoked', 'created_at'
        ]
        read_only_fields = ['id', 'code', 'used_count', 'created_at', 'empresa', 'empresa_name']

    def validate_expires_at(self, value):
        if value and value < timezone.now() + timezone.timedelta(minutes=15):
            raise serializers.ValidationError("La vigencia mínima del código es 15 minutos.")
        return value

    def validate_manual_code(self, value):
        # normalizar y validar unicidad
        code = value.strip().upper()
        if JoinCode.objects.filter(code=code).exists():
            raise serializers.ValidationError("Código ya existe.")
        # comprobación simple de formato (solo alfanumérico)
        if not code.isalnum():
            raise serializers.ValidationError("Código sólo debe contener letras y números.")
        if len(code) > 16:
            raise serializers.ValidationError("Código demasiado largo (máx 16).")
        return code

    def create(self, validated_data):
        request = self.context.get('request')
        # asignar empresa y created_by al crear por API
        if not request or not request.user or not getattr(request.user, 'empresa', None):
            raise serializers.ValidationError("Usuario no tiene empresa asignada.")
        validated_data['empresa'] = request.user.empresa
        validated_data['created_by'] = request.user

        # si el cliente pasó manual_code, véalo y setéalo como 'code' antes de save
        manual = validated_data.pop('manual_code', None)
        if manual:
            validated_data['code'] = manual.strip().upper()
        # dejar que el model genere un código si no hay manual
        return super().create(validated_data)


class ValidateJoinCodeSerializer(serializers.Serializer):
    code = serializers.CharField(max_length=16)

    def validate_code(self, value):
        code = value.strip().upper()
        if not JoinCode.objects.filter(code=code).exists():
            raise serializers.ValidationError("Código no encontrado.")
        return code
