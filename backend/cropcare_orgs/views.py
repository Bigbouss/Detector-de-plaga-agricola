"""
cropcare_orgs/views.py
Vistas para registro de empresas, usuarios administradores/trabajadores
y gestión de códigos de invitación (JoinCode).
"""

from django.contrib.auth import get_user_model
from django.db import transaction

from rest_framework import generics, status, viewsets, mixins, permissions
from rest_framework.decorators import action
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Empresa, EmployeeProfile, JoinCode
from .serializers import (
    EmpresaSerializer,
    RegisterAdminSerializer,
    RegisterWorkerSerializer,
    JoinCodeSerializer,
    MeSerializer,
    UserPublicSerializer,
    ProfileSerializer,
)
from .permissions import IsCompanyAdmin

# Perfil “personal” (no empresarial) definido en la app api
from api.models import UserProfile

User = get_user_model()


class RegisterAdminView(generics.GenericAPIView):
    """
    Registro de ADMIN: crea usuario, empresa y perfil ADMIN.
    Devuelve tokens JWT además de los datos creados.
    """
    permission_classes = [AllowAny]
    serializer_class = RegisterAdminSerializer

    def post(self, request, *args, **kwargs):
        ser = self.get_serializer(data=request.data)
        ser.is_valid(raise_exception=True)
        user = ser.save()
        return Response(ser.to_representation(user), status=status.HTTP_201_CREATED)


class RegisterWorkerView(generics.GenericAPIView):
    """
    Registro de WORKER usando un join_code válido.
    Devuelve tokens JWT además de los datos creados.
    """
    permission_classes = [AllowAny]
    serializer_class = RegisterWorkerSerializer

    def post(self, request, *args, **kwargs):
        ser = self.get_serializer(data=request.data)
        ser.is_valid(raise_exception=True)
        user = ser.save()
        return Response(ser.to_representation(user), status=status.HTTP_201_CREATED)


class JoinCodeViewSet(
    mixins.CreateModelMixin,
    mixins.ListModelMixin,
    mixins.RetrieveModelMixin,
    mixins.UpdateModelMixin,
    viewsets.GenericViewSet,
):
    """
    CRUD parcial de códigos de invitación (crear/listar/ver/actualizar).
    Restringido a ADMIN de la empresa actual.
    """
    serializer_class = JoinCodeSerializer
    permission_classes = [IsAuthenticated, IsCompanyAdmin]

    def get_queryset(self):
        # Códigos de la empresa del usuario (ADMIN actual)
        return JoinCode.objects.filter(
            empresa=self.request.user.profile.empresa
        ).order_by("-created_at")


class CompanyWorkersViewSet(viewsets.ViewSet):
    """
    Gestión de trabajadores (sólo ADMIN):
    - GET: listar workers de la empresa
    - POST: crear usuario worker directo (sin join code)
    """
    permission_classes = [IsAuthenticated, IsCompanyAdmin]

    def list(self, request):
        empresa = request.user.profile.empresa
        workers = EmployeeProfile.objects.select_related("user").filter(
            empresa=empresa, role=EmployeeProfile.Role.WORKER
        )
        data = [
            {
                "id": w.user.id,
                "user": UserPublicSerializer(w.user).data,
                "profile": ProfileSerializer(w).data,
            }
            for w in workers
        ]
        return Response(data)

    @transaction.atomic
    def create(self, request):
        """Crear trabajador directo (sin código)."""
        email = (request.data.get("email") or "").lower().strip()
        password = request.data.get("password")
        first_name = request.data.get("first_name", "")
        last_name = request.data.get("last_name", "")

        if not email or not password:
            return Response(
                {"detail": "email y password son obligatorios."}, status=400
            )
        if User.objects.filter(email=email).exists():
            return Response(
                {"detail": "Ya existe un usuario con ese email."}, status=400
            )

        # Crear usuario
        user = User.objects.create_user(
            username=email,
            email=email,
            first_name=first_name,
            last_name=last_name,
            password=password,
        )
        # Asociarlo como WORKER de la empresa del admin
        empresa = request.user.profile.empresa
        EmployeeProfile.objects.create(
            user=user, empresa=empresa, role=EmployeeProfile.Role.WORKER
        )
        # Perfil “personal” para datos no empresariales
        UserProfile.objects.create(
            user=user, display_name=f"{first_name} {last_name}".strip()
        )
        return Response(
            {
                "user": UserPublicSerializer(user).data,
                "profile": ProfileSerializer(user.profile).data,
            },
            status=201,
        )


class MeView(generics.GenericAPIView):
    """
    Información del usuario autenticado (datos + perfil + empresa).
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        return Response(MeSerializer.build(request.user))


class JoinCompanyView(APIView):
    """
    Unirse a una empresa usando un join_code.
    Regla: sólo usuarios SIN empresa pueden canjear código.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        code_str = (request.data.get("join_code") or "").strip()
        if not code_str:
            return Response({"detail": "join_code es requerido."}, status=400)

        # Bloquear si el usuario ya pertenece a una empresa
        profile = getattr(request.user, "profile", None)
        if profile and profile.empresa_id:
            return Response(
                {
                    "detail": "Ya perteneces a una empresa. "
                    "Debes salir de tu empresa actual antes de unirte a otra."
                },
                status=400,
            )

        try:
            jc = JoinCode.objects.select_related("empresa").get(code=code_str)
        except JoinCode.DoesNotExist:
            return Response({"detail": "Código inválido."}, status=400)

        if not jc.is_valid:
            return Response(
                {"detail": "Código expirado, revocado o sin cupos."}, status=400
            )

        # Redimir: pasa a WORKER de esa empresa
        profile = jc.redeem_for(request.user)
        return Response(
            {
                "message": "Te uniste a la empresa exitosamente.",
                "role": profile.role,
                "empresa": profile.empresa.name,
            },
            status=200,
        )
