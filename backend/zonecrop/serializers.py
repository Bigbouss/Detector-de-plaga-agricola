from rest_framework import serializers
from .models import Zona, Cultivo, WorkerZoneAssignment
from django.db import transaction

class CultivoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Cultivo
        fields = ['id', 'nombre', 'zona', 'created_at', 'updated_at']
        read_only_fields = ['id', 'created_at', 'updated_at']

    def validate_nombre(self, v):
        v = v.strip()
        if len(v) == 0:
            raise serializers.ValidationError("Nombre inválido.")
        return v

    def validate(self, attrs):
        zona = attrs.get('zona') or getattr(self.instance, 'zona', None)
        nombre = attrs.get('nombre') or getattr(self.instance, 'nombre', None)
        if zona and nombre:
            qs = Cultivo.objects.filter(zona=zona, nombre__iexact=nombre)
            if self.instance:
                qs = qs.exclude(pk=self.instance.pk)
            if qs.exists():
                raise serializers.ValidationError({"nombre": "Ya existe un cultivo con ese nombre en la zona."})
        return attrs


class ZonaSerializer(serializers.ModelSerializer):
    cultivos = CultivoSerializer(many=True, read_only=True)
    cultivos_count = serializers.IntegerField(source='cultivos.count', read_only=True)

    class Meta:
        model = Zona
        fields = ['id', 'nombre', 'descripcion', 'empresa', 'cultivos', 'cultivos_count', 'created_at', 'updated_at']
        read_only_fields = ['id', 'empresa', 'created_at', 'updated_at']

    def validate_nombre(self, v):
        return v.strip()

    def validate(self, attrs):
        # controlar unicidad por empresa en serializer (ayuda a front)
        request = self.context.get('request')
        nombre = attrs.get('nombre') or getattr(self.instance, 'nombre', None)
        empresa = attrs.get('empresa') or getattr(self.instance, 'empresa', None) or (request.user.empresa if request and hasattr(request.user, 'empresa') else None)
        if nombre and empresa:
            qs = Zona.objects.filter(nombre__iexact=nombre, empresa=empresa)
            if self.instance:
                qs = qs.exclude(pk=self.instance.pk)
            if qs.exists():
                raise serializers.ValidationError({'nombre': 'Ya existe una zona con este nombre en tu empresa.'})
        return attrs

    def create(self, validated_data):
        # Forzar empresa del request.user si existe (evita que pasen empresa en el body)
        request = self.context.get('request')
        if request and hasattr(request.user, 'empresa') and request.user.empresa:
            validated_data['empresa'] = request.user.empresa
        return super().create(validated_data)


class WorkerZoneAssignmentSerializer(serializers.ModelSerializer):
    worker_email = serializers.CharField(source='worker.email', read_only=True)
    zona_nombre = serializers.CharField(source='zona.nombre', read_only=True)

    class Meta:
        model = WorkerZoneAssignment
        fields = ['id', 'worker', 'worker_email', 'zona', 'zona_nombre', 'assigned_at']
        read_only_fields = ['id', 'assigned_at', 'worker_email', 'zona_nombre']

    def validate(self, attrs):
        worker = attrs.get('worker')
        zona = attrs.get('zona')
        # Reglas de negocio:
        if worker is None or zona is None:
            raise serializers.ValidationError("worker y zona son obligatorios.")
        # worker debe pertenecer a la misma empresa
        if getattr(worker, 'empresa_id', None) != zona.empresa_id:
            raise serializers.ValidationError("El trabajador debe pertenecer a la misma empresa de la zona.")
        return attrs
