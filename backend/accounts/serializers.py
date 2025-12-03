# accounts/serializers.py
from rest_framework import serializers
from django.contrib.auth import get_user_model
from django.db import transaction
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from django.apps import apps
from .models import Role
from django.core.exceptions import ValidationError as DjangoValidationError
User = get_user_model()

class RoleSerializer(serializers.ModelSerializer):
    class Meta:
        model = Role
        fields = ['id', 'code', 'name']


class UserSerializer(serializers.ModelSerializer):
    role = RoleSerializer(read_only=True)

    class Meta:
        model = User
        fields = ['id', 'email', 'first_name', 'last_name', 'phone', 'role', 'empresa', 'is_active', 'created_at', 'updated_at']
        read_only_fields = ['id', 'created_at', 'updated_at', 'is_active']


class UserCreateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)
    role_code = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ['email', 'first_name', 'last_name', 'phone', 'password', 'role_code', 'empresa']

    @transaction.atomic
    def create(self, validated_data):
        # aquí validated_data ya contiene '_join_code_obj' (de validate())
        join_code = validated_data.pop('_join_code_obj')
        email = validated_data.pop('email')
        password = validated_data.pop('password')
        first_name = validated_data.pop('first_name')
        last_name = validated_data.pop('last_name')
        phone = validated_data.get('phone', '')

        # obtener role WORKER (debe existir en seed_roles)
        role = Role.objects.get(code='WORKER')

        # 1) crear el usuario ya asignado a la empresa del join_code (cumple validaciones)
        # usamos create_user (que a su vez llama save() y full_clean()):
        user = User.objects.create_user(
            email=email,
            password=password,
            first_name=first_name,
            last_name=last_name,
            phone=phone,
            role=role,
            empresa=join_code.empresa,
        )

        # 2) consumir el join code de forma atómica: incrementar used_count y crear usage
        if not join_code.is_valid:
            # reversar la creación para evitar usuarios huérfanos (saldrá por la transacción)
            raise serializers.ValidationError({"join_code": "El código no es válido al intentar consumirlo."})

        # incrementar contador de forma segura y crear registro de uso
        # usar F() evita race conditions en concurrencia simple (mejor en DBs que soportan)
        from django.db.models import F
        join_code.used_count = F('used_count') + 1
        join_code.save(update_fields=['used_count'])
        # refrescar para leer el integer real
        join_code.refresh_from_db(fields=['used_count'])

        # crear JoinCodeUsage (silencioso si existe constraint unique, permitimos excepción)
        try:
            JoinCodeUsage = apps.get_model('joincodes', 'JoinCodeUsage')
        except LookupError:
            JoinCodeUsage = None

        if JoinCodeUsage:
            JoinCodeUsage.objects.create(join_code=join_code, user=user)

        return user


class RegisterAdminSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(min_length=8, write_only=True)
    first_name = serializers.CharField()
    last_name = serializers.CharField()
    phone = serializers.CharField(required=False, allow_blank=True)
    company_name = serializers.CharField()
    tax_id = serializers.CharField()

    def validate_email(self, value):
        if User.objects.filter(email=value.lower()).exists():
            raise serializers.ValidationError("Ya existe un usuario con ese email.")
        return value.lower()

    @transaction.atomic
    def create(self, validated_data):
        """
        Crea usuario ADMIN + empresa atomically (ambos o ninguno).
        Proceso:
        1. Crear empresa sin owner (temporalmente null)
        2. Crear usuario ADMIN con la empresa
        3. Asignar el usuario como owner de la empresa
        """
        from emprises.models import Empresa

        email = validated_data['email']
        password = validated_data['password']
        first_name = validated_data['first_name']
        last_name = validated_data['last_name']
        phone = validated_data.get('phone', '')
        company_name = validated_data['company_name']
        tax_id = validated_data['tax_id']

        # Obtener role ADMIN
        role = Role.objects.get(code='ADMIN')

        # Crear Empresa sin owner (owner=None temporalmente)
        empresa = Empresa.objects.create(
            name=company_name,
            legal_name=company_name,
            tax_id=tax_id,
            country='CL',
            owner=None
        )

        # Crear Usuario ADMIN con la empresa
        user = User.objects.create_user(
            email=email,
            password=password,
            first_name=first_name,
            last_name=last_name,
            phone=phone,
            role=role,
            empresa=empresa,
            is_staff=True
        )

        # Asignar el usuario como owner de la empresa
        empresa.owner = user
        empresa.save()

        return user

class RegisterWorkerSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(min_length=8, write_only=True)
    first_name = serializers.CharField()
    last_name = serializers.CharField()
    phone = serializers.CharField(required=False, allow_blank=True)
    join_code = serializers.CharField()

    def validate_email(self, value):
        if User.objects.filter(email=value.lower()).exists():
            raise serializers.ValidationError("Ya existe un usuario con ese email.")
        return value.lower()

    def validate(self, attrs):
        # buscamos el modelo JoinCode de forma segura (evita ImportError)
        JoinCode = None
        for label in ('joincodes', 'joinCodes'):
            try:
                JoinCode = apps.get_model(label, 'JoinCode')
                break
            except LookupError:
                continue

        if JoinCode is None:
            raise serializers.ValidationError({'join_code': 'El módulo de JoinCodes no está instalado en el servidor.'})

        code = attrs.get('join_code', '').strip().upper()
        try:
            jc = JoinCode.objects.select_related('empresa').get(code=code)
        except JoinCode.DoesNotExist:
            raise serializers.ValidationError({'join_code': 'Código inválido.'})

        # usa la propiedad is_valid del modelo para comprobar vigencia/cupos
        if not getattr(jc, 'is_valid', True):
            raise serializers.ValidationError({'join_code': 'Código expirado o sin cupos.'})

        attrs['_join_code_obj'] = jc
        return attrs

    @transaction.atomic
    def create(self, validated_data):
        join_code = validated_data.pop('_join_code_obj')
        email = validated_data.pop('email')
        password = validated_data.pop('password')
        first_name = validated_data.pop('first_name')
        last_name = validated_data.pop('last_name')
        phone = validated_data.get('phone', '')

        role = Role.objects.get(code='WORKER')
        user = User.objects.create_user(
            email=email,
            password=password,
            first_name=first_name,
            last_name=last_name,
            phone=phone,
            role=role,
            empresa=join_code.empresa,
        )
        # redimir el código (se encarga de aumentar used_count y crear JoinCodeUsage)
        # se asume que JoinCode tiene método redeem_for(user)
        join_code.redeem_for(user)
        return user

# JWT custom serializer: añade claims extras
class MyTokenObtainPairSerializer(TokenObtainPairSerializer):
    @classmethod
    def get_token(cls, user):
        token = super().get_token(user)
        token['email'] = user.email
        token['role'] = user.role.code if user.role_id else None
        token['empresa_id'] = user.empresa_id
        return token
