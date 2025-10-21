"""
api/serializers.py
Serializadores de datos para la aplicación API de CropCare.
"""
from django.contrib.auth import get_user_model
from django.db import transaction
from django.utils import timezone
from rest_framework import serializers
from rest_framework_simplejwt.tokens import RefreshToken

from .models import Cultivo, Zona, Plot, Inspection, Diagnostic, Report, UserProfile
from cropcare_orgs.models import EmployeeProfile, Empresa

User = get_user_model()


class ZonaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Zona
        fields = ['id', 'nombre', 'cultivo', 'empresa', 'created_at', 'updated_at']
        read_only_fields = ['id', 'empresa', 'created_at', 'updated_at']

    def validate(self, attrs):
        """
        Validaciones adicionales para Zona:
        - El nombre debe ser único dentro de la misma empresa (en Meta.unique_together).
        """
        # No se agregan validaciones personalizadas adicionales aquí,
        # pues unique_together será validado automáticamente en el modelo.
        return attrs


class PlotSerializer(serializers.ModelSerializer):
    class Meta:
        model = Plot
        fields = ['id', 'name', 'zona', 'cultivo', 'owner', 'notes', 'created_at', 'updated_at']
        read_only_fields = ['id', 'owner', 'created_at', 'updated_at']

    def validate(self, attrs):
        """
        Validación cruzada de zona vs. rol del usuario.
        Se asegura que:
        - Si el usuario pertenece a una empresa, proporcione una zona válida de su empresa.
        - Si el usuario es individual, no se incluya zona.
        """
        user = self.context['request'].user
        profile = getattr(user, 'profile', None)
        zona = attrs.get('zona')
        if profile and profile.empresa_id:
            # Usuario de empresa: debe especificar zona (de su empresa)
            if zona is None:
                raise serializers.ValidationError("Debes asignar la parcela a una zona de tu empresa.")
            if zona.empresa_id != profile.empresa_id:
                raise serializers.ValidationError("La zona seleccionada no pertenece a tu empresa.")
        else:
            # Usuario individual: no debe enviar zona
            if zona is not None:
                raise serializers.ValidationError("Los usuarios individuales no pueden asignar la parcela a una zona.")
        return attrs

    def validate_notes(self, value):
        # Validar número de palabras en notes (Plot) - máximo 120 palabras
        if value:
            num_words = len(value.split())
            if num_words > 120:
                raise serializers.ValidationError("Las notas de la parcela no pueden exceder 120 palabras.")
        return value


class InspectionSerializer(serializers.ModelSerializer):
    class Meta:
        model = Inspection
        fields = ['id', 'plot', 'owner', 'image', 'notes', 'inspected_at']
        read_only_fields = ['id', 'owner', 'inspected_at']

    def validate(self, attrs):
        """
        Validación de que la parcela pertenece al usuario o a su empresa.
        """
        user = self.context['request'].user
        plot = attrs.get('plot')
        if not plot:
            raise serializers.ValidationError("Debe especificar una parcela para la inspección.")
        profile = getattr(user, 'profile', None)
        if profile and profile.empresa_id:
            # Usuario de empresa: la parcela debe ser de su empresa
            if plot.zona is None or plot.zona.empresa_id != profile.empresa_id:
                raise serializers.ValidationError("No puedes inspeccionar una parcela fuera de tu empresa.")
        else:
            # Usuario individual: la parcela debe ser propia
            if plot.owner_id != user.id:
                raise serializers.ValidationError("No puedes inspeccionar parcelas que no son tuyas.")
        return attrs

    def validate_notes(self, value):
        # Validar número de palabras en notes (Inspection) - máximo 240 palabras
        if value:
            num_words = len(value.split())
            if num_words > 240:
                raise serializers.ValidationError("Las notas de la inspección no pueden exceder 240 palabras.")
        return value


class DiagnosticSerializer(serializers.ModelSerializer):
    class Meta:
        model = Diagnostic
        fields = ['id', 'inspection', 'owner', 'label', 'confidence', 'model_version', 'created_at']
        read_only_fields = ['id', 'owner', 'created_at']


class ReportSerializer(serializers.ModelSerializer):
    class Meta:
        model = Report
        fields = ['id', 'title', 'description', 'format', 'status', 'owner', 'generated_at', 'created_at']
        read_only_fields = ['id', 'owner', 'status', 'generated_at', 'created_at']


class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ['id', 'display_name', 'organization', 'phone', 'created_at']
        read_only_fields = ['id', 'created_at']


class RegisterSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True, min_length=8)
    first_name = serializers.CharField(required=False, allow_blank=True)
    last_name = serializers.CharField(required=False, allow_blank=True)
    username = serializers.CharField(required=False, allow_blank=True)

    def validate(self, attrs):
        email = attrs['email'].lower()
        if User.objects.filter(email=email).exists():
            raise serializers.ValidationError("Ya existe un usuario con ese email.")
        return attrs

    @transaction.atomic
    def create(self, validated_data):
        email = validated_data['email'].lower()
        password = validated_data['password']
        username = validated_data.get('username') or email
        first_name = validated_data.get('first_name', '')
        last_name = validated_data.get('last_name', '')
        # Crear el nuevo usuario
        user = User.objects.create_user(
            username=username,
            email=email,
            first_name=first_name,
            last_name=last_name,
            password=password,
        )
        # Crear perfil de empleado INDIVIDUAL
        EmployeeProfile.objects.create(user=user, role=EmployeeProfile.Role.INDIVIDUAL)
        # Crear perfil de usuario personal
        UserProfile.objects.create(user=user, display_name=f"{first_name} {last_name}".strip())
        return user

    def to_representation(self, instance):
        # Generar tokens JWT para el nuevo usuario y retornar datos relevantes
        refresh = RefreshToken.for_user(instance)
        profile = getattr(instance, 'profile', None)
        empresa = profile.empresa if profile and profile.empresa_id else None
        return {
            'user': {
                'id': instance.id,
                'username': instance.username,
                'email': instance.email,
                'first_name': instance.first_name,
                'last_name': instance.last_name,
            },
            'profile': {
                'role': profile.role,
                'empresa': profile.empresa.name if profile.empresa_id else None
            } if profile else None,
            'tokens': {
                'access': str(refresh.access_token),
                'refresh': str(refresh),
            }
        }
