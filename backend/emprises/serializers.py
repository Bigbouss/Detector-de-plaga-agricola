from rest_framework import serializers
from django.db import transaction
from django.conf import settings
from .models import Empresa

User = settings.AUTH_USER_MODEL

class EmpresaSerializer(serializers.ModelSerializer):
    owner_id = serializers.PrimaryKeyRelatedField(source='owner', read_only=True)

    class Meta:
        model = Empresa
        fields = ['id', 'tax_id', 'name', 'legal_name', 'country', 'owner_id', 'created_at', 'updated_at']
        read_only_fields = ['id', 'owner_id', 'created_at', 'updated_at']

class EmpresaCreateSerializer(serializers.ModelSerializer):
    """
    Usado desde una vista donde el request.user será el owner (ADMIN).
    No permitimos pasar owner por body: se usa request.user.
    """
    class Meta:
        model = Empresa
        fields = ['tax_id', 'name', 'legal_name', 'country']

    def validate_tax_id(self, value):
        v = value.strip().upper()
        if len(v) > 12:
            raise serializers.ValidationError("tax_id no puede exceder 12 caracteres.")
        return v

    def create(self, validated_data):
        request = self.context.get('request')
        if request is None or not hasattr(request, 'user'):
            raise serializers.ValidationError("No se pudo determinar el owner.")
        owner = request.user

        # validación de role: si el user tiene role, exigir ADMIN aquí
        role = getattr(owner, 'role', None)
        if role and getattr(role, 'code', None) != 'ADMIN':
            raise serializers.ValidationError("Solo usuarios ADMIN pueden crear empresas.")

        with transaction.atomic():
            empresa = Empresa.objects.create(owner=owner, **validated_data)
            # no hacemos más acciones aquí; joinCode y demás quedan en su app
            return empresa
