"""
cropcare_orgs/serializers.py
Serializadores para la aplicación de gestión de organizaciones (empresas, empleados, códigos de unión).
"""
from django.contrib.auth import get_user_model
from django.db import transaction
from django.utils import timezone
from rest_framework import serializers
from rest_framework_simplejwt.tokens import RefreshToken

from .models import Empresa, EmployeeProfile, JoinCode

User = get_user_model()


class EmpresaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Empresa
        fields = ['id', 'name', 'legal_name', 'tax_id', 'country', 'timezone', 'owner', 'created_at']
        read_only_fields = ['id', 'owner', 'created_at']


class UserPublicSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'first_name', 'last_name']
        read_only_fields = ['id']


class ProfileSerializer(serializers.ModelSerializer):
    user = UserPublicSerializer(read_only=True)
    empresa = EmpresaSerializer(read_only=True)

    class Meta:
        model = EmployeeProfile
        fields = ['user', 'role', 'empresa', 'is_active', 'can_manage_plots', 'joined_at']


class RegisterAdminSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True, min_length=8)
    first_name = serializers.CharField(required=False, allow_blank=True)
    last_name = serializers.CharField(required=False, allow_blank=True)
    username = serializers.CharField(required=False, allow_blank=True)
    company_name = serializers.CharField()
    tax_id = serializers.CharField(required=False, allow_blank=True)

    def validate(self, attrs):
        email = attrs['email'].lower()
        if User.objects.filter(email=email).exists():
            raise serializers.ValidationError('Ya existe un usuario con ese email.')
        return attrs

    @transaction.atomic
    def create(self, validated_data):
        email = validated_data['email'].lower()
        password = validated_data['password']
        username = validated_data.get('username') or email
        first_name = validated_data.get('first_name', '')
        last_name = validated_data.get('last_name', '')
        company_name = validated_data['company_name']
        tax_id = validated_data.get('tax_id', '')
        # Crear usuario administrador
        user = User.objects.create_user(
            username=username,
            email=email,
            first_name=first_name,
            last_name=last_name,
            password=password,
        )
        # Crear empresa
        empresa = Empresa.objects.create(
            name=company_name,
            tax_id=tax_id,
            owner=user,
        )
        # Crear perfil de empleado ADMIN
        EmployeeProfile.objects.create(user=user, empresa=empresa, role=EmployeeProfile.Role.ADMIN,
                                       can_manage_plots=True)
        # Crear perfil de usuario personal con nombre de empresa como organización
        from api.models import UserProfile
        UserProfile.objects.create(user=user, display_name=f"{first_name} {last_name}".strip(),
                                   organization=company_name)
        return user

    def to_representation(self, instance):
        # Incluir datos del usuario, perfil, empresa y tokens JWT
        refresh = RefreshToken.for_user(instance)
        profile = getattr(instance, 'profile', None)
        empresa = profile.empresa if profile and profile.empresa_id else None
        return {
            'user': UserPublicSerializer(instance).data,
            'profile': ProfileSerializer(profile).data if profile else None,
            'empresa': EmpresaSerializer(empresa).data if empresa else None,
            'tokens': {
                'access': str(refresh.access_token),
                'refresh': str(refresh),
            }
        }


class RegisterWorkerSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True, min_length=8)
    first_name = serializers.CharField(required=False, allow_blank=True)
    last_name = serializers.CharField(required=False, allow_blank=True)
    username = serializers.CharField(required=False, allow_blank=True)
    join_code = serializers.CharField()

    def validate(self, attrs):
        email = attrs['email'].lower()
        if User.objects.filter(email=email).exists():
            raise serializers.ValidationError('Ya existe un usuario con ese email.')
        try:
            code = JoinCode.objects.select_related('empresa').get(code=attrs['join_code'])
        except JoinCode.DoesNotExist:
            raise serializers.ValidationError('Código inválido.')
        if not code.is_valid:
            raise serializers.ValidationError('Código expirado o sin cupos.')
        attrs['join_code_obj'] = code
        return attrs

    @transaction.atomic
    def create(self, validated_data):
        email = validated_data['email'].lower()
        password = validated_data['password']
        username = validated_data.get('username') or email
        first_name = validated_data.get('first_name', '')
        last_name = validated_data.get('last_name', '')
        code: JoinCode = validated_data['join_code_obj']
        # Crear nuevo usuario
        user = User.objects.create_user(
            username=username,
            email=email,
            first_name=first_name,
            last_name=last_name,
            password=password,
        )
        # Unir a la empresa usando el código (asigna perfil WORKER)
        code.redeem_for(user)
        # Crear perfil de usuario personal con nombre de empresa como organización
        from api.models import UserProfile
        UserProfile.objects.create(user=user, display_name=f"{first_name} {last_name}".strip(),
                                   organization=code.empresa.name)
        return user

    def to_representation(self, instance):
        refresh = RefreshToken.for_user(instance)
        profile = getattr(instance, 'profile', None)
        empresa = profile.empresa if profile and profile.empresa_id else None
        return {
            'user': UserPublicSerializer(instance).data,
            'profile': ProfileSerializer(profile).data if profile else None,
            'empresa': EmpresaSerializer(empresa).data if empresa else None,
            'tokens': {
                'access': str(refresh.access_token),
                'refresh': str(refresh),
            }
        }


class JoinCodeSerializer(serializers.ModelSerializer):
    class Meta:
        model = JoinCode
        fields = ['id', 'code', 'empresa', 'role', 'max_uses', 'used_count', 'expires_at', 'revoked', 'created_at']
        read_only_fields = ['id', 'code', 'empresa', 'used_count', 'created_at']

    def validate_expires_at(self, value):
        if value and value < timezone.now() + timezone.timedelta(minutes=15):
            raise serializers.ValidationError('La vigencia mínima del código es 15 minutos.')
        return value

    def create(self, validated_data):
        # Asignar empresa y creador actuales al crear un código
        user = self.context['request'].user
        profile = user.profile
        validated_data['empresa'] = profile.empresa
        validated_data['created_by'] = user
        return super().create(validated_data)


class MeSerializer(serializers.Serializer):
    user = UserPublicSerializer()
    profile = ProfileSerializer()

    @classmethod
    def build(cls, user):
        profile = getattr(user, 'profile', None)
        data = {
            'user': UserPublicSerializer(user).data,
            'profile': ProfileSerializer(profile).data if profile else None,
        }
        # Si el usuario pertenece a una empresa, incluir datos de la empresa
        if profile and profile.empresa_id:
            data['empresa'] = EmpresaSerializer(profile.empresa).data
        return data
