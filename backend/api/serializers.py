# api/serializers.py
from __future__ import annotations

from datetime import date
from decimal import Decimal

from django.contrib.auth import get_user_model
from rest_framework import serializers

from .models import Plot, Inspection, Diagnostic, Report, UserProfile

User = get_user_model()


# ------------------ Auth / Perfil ------------------

class UserRegisterSerializer(serializers.ModelSerializer):
    """
    Registro de usuario básico.
    """
    password = serializers.CharField(write_only=True, min_length=6)

    class Meta:
        model = User
        fields = ["id", "username", "email", "password"]

    def create(self, validated_data):
        # Crea el usuario con hash de contraseña
        return User.objects.create_user(
            username=validated_data["username"],
            email=validated_data.get("email", ""),
            password=validated_data["password"],
        )


class UserProfileSerializer(serializers.ModelSerializer):
    """
    Perfil del usuario autenticado.
    'user' no se expone para escritura.
    """
    class Meta:
        model = UserProfile
        fields = ["id", "created_at", "updated_at", "display_name", "organization", "phone"]
        read_only_fields = ["id", "created_at", "updated_at"]


# ------------------ Mixins útiles ------------------

class OwnerHiddenMixin(serializers.Serializer):
    """
    Inyecta el 'owner' a partir del usuario autenticado.
    Todos los serializers de entidades de negocio lo heredan.
    """
    owner = serializers.HiddenField(default=serializers.CurrentUserDefault())


# ------------------ Plot ------------------

class PlotSerializer(OwnerHiddenMixin, serializers.ModelSerializer):
    class Meta:
        model = Plot
        fields = [
            "id", "created_at", "updated_at",
            "name", "cultivo", "superficie_ha", "fecha_siembra", "notes",
            "owner",
        ]
        read_only_fields = ["id", "created_at", "updated_at"]

    # Validaciones de negocio a nivel serializer:
    def validate(self, attrs):
        """
        Requisitos:
          - superficie_ha > 0
          - fecha_siembra no puede estar en el futuro
          - name único por usuario (case-insensitive)
        """
        user = self.context["request"].user

        # superficie > 0
        superficie = attrs.get("superficie_ha", getattr(self.instance, "superficie_ha", None))
        if superficie is not None and Decimal(superficie) <= Decimal("0"):
            raise serializers.ValidationError({"superficie_ha": "Debe ser mayor a 0."})

        # fecha de siembra no futura
        f = attrs.get("fecha_siembra", getattr(self.instance, "fecha_siembra", None))
        if f is not None and f > date.today():
            raise serializers.ValidationError({"fecha_siembra": "No puede ser una fecha futura."})

        # nombre único por owner (ignorando mayúsculas/minúsculas)
        name = attrs.get("name", getattr(self.instance, "name", None))
        if name:
            qs = Plot.objects.filter(owner=user, name__iexact=name)
            if self.instance:
                qs = qs.exclude(pk=self.instance.pk)
            if qs.exists():
                raise serializers.ValidationError({"name": "Ya tienes un lote con ese nombre."})

        return attrs


# ------------------ Inspection ------------------

class InspectionSerializer(OwnerHiddenMixin, serializers.ModelSerializer):
    class Meta:
        model = Inspection
        fields = [
            "id", "created_at", "updated_at",
            "plot", "inspected_at", "notes", "photo",
            "owner",
        ]
        read_only_fields = ["id", "created_at", "updated_at", "inspected_at"]

    def validate_plot(self, plot: Plot):
        """
        El 'plot' debe ser del mismo owner (usuario autenticado).
        """
        user = self.context["request"].user
        if plot.owner_id != user.id:
            raise serializers.ValidationError("No puedes usar una parcela que no es tuya.")
        return plot


# ------------------ Diagnostic ------------------

class DiagnosticSerializer(OwnerHiddenMixin, serializers.ModelSerializer):
    class Meta:
        model = Diagnostic
        fields = [
            "id", "created_at", "updated_at",
            "inspection", "label", "confidence", "model_version", "device_id",
            "owner",
        ]
        read_only_fields = ["id", "created_at", "updated_at", "model_version", "device_id"]

    def validate_inspection(self, inspection: "Inspection"):
        user = self.context["request"].user
        if inspection.owner_id != user.id:
            raise serializers.ValidationError("No puedes diagnosticar una inspección ajena.")
        return inspection

    def validate_confidence(self, value):
        if value < Decimal("0") or value > Decimal("1"):
            raise serializers.ValidationError("La confianza debe estar entre 0 y 1.")
        return value


# ------------------ Report ------------------

class ReportSerializer(OwnerHiddenMixin, serializers.ModelSerializer):
    class Meta:
        model = Report
        fields = [
            "id", "created_at", "updated_at",
            "title", "description", "format", "status", "generated_at", "file",
            "owner",
        ]
        read_only_fields = ["id", "created_at", "updated_at", "generated_at", "file"]
