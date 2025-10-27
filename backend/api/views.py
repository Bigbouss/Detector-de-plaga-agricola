"""
api/views.py
Vistas de la aplicación API de CropCare.
"""
from django.contrib.auth import get_user_model
from django.db.models import Q
from rest_framework import generics, viewsets, mixins, permissions
from rest_framework.decorators import action
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Zona, Plot, Inspection, Diagnostic, Report, UserProfile
from .serializers import (
    ZonaSerializer,
    PlotSerializer,
    InspectionSerializer,
    DiagnosticSerializer,
    ReportSerializer,
    RegisterSerializer,
    UserProfileSerializer,
)
from .permissions import CanCreateZona, CanCreatePlot

User = get_user_model()


class ApiRootView(APIView):
    """
    Vista raíz de la API que proporciona enlaces o información general.
    """
    permission_classes = [AllowAny]

    def get(self, request):
        return Response({
            "message": "Bienvenido a CropCare API",
            "auth_register": request.build_absolute_uri('/api/auth/register/'),
            "auth_login": request.build_absolute_uri('/api/auth/jwt/create/'),
            "profile": request.build_absolute_uri('/api/auth/profile/'),
            "plots": request.build_absolute_uri('/api/plots/'),
            "inspections": request.build_absolute_uri('/api/inspections/'),
            "reports": request.build_absolute_uri('/api/reports/')
        })


class PingView(APIView):
    """
    Vista para comprobar la autenticación JWT (responde 'pong' si el token es válido).
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        return Response({"detail": "pong"})


class RegisterView(generics.GenericAPIView):
    """
    Registro de usuario individual (sin empresa).
    """
    permission_classes = [AllowAny]
    serializer_class = RegisterSerializer

    def post(self, request, *args, **kwargs):
        ser = self.get_serializer(data=request.data)
        ser.is_valid(raise_exception=True)
        user = ser.save()
        # Se devuelve el usuario con su perfil y tokens de autenticación
        return Response(ser.to_representation(user), status=201)


class UserProfileMeView(generics.RetrieveUpdateAPIView):
    """
    Obtener o actualizar el perfil personal del usuario autenticado (display_name, phone, etc.).
    """
    permission_classes = [IsAuthenticated]
    serializer_class = UserProfileSerializer

    def get_object(self):
        # Retorna el perfil de usuario (UserProfile) del usuario autenticado
        return self.request.user.userprofile


class ZonaViewSet(viewsets.ModelViewSet):
    """
    ViewSet para CRUD de Zonas (solo para usuarios de empresa con permisos adecuados).
    """
    permission_classes = [IsAuthenticated, CanCreateZona]
    serializer_class = ZonaSerializer

    def get_queryset(self):
        # Usuarios de empresa: filtrar zonas por su empresa. Usuarios individuales: ninguno.
        user = self.request.user
        profile = getattr(user, 'profile', None)
        if profile and profile.empresa_id:
            # Zonas de la empresa del usuario
            return Zona.objects.filter(empresa=profile.empresa).order_by('-created_at')
        # Si es usuario individual (sin empresa), no tiene zonas
        return Zona.objects.none()

    def perform_create(self, serializer):
        # Asigna la empresa del usuario a la zona que se crea
        empresa = self.request.user.profile.empresa
        serializer.save(empresa=empresa)


class PlotViewSet(viewsets.ModelViewSet):
    """
    ViewSet para CRUD de Parcelas (Plots).
    Admins y trabajadores con permiso pueden crear/editar parcelas en la empresa;
    usuarios individuales pueden gestionar sus parcelas personales.
    """
    permission_classes = [IsAuthenticated, CanCreatePlot]
    serializer_class = PlotSerializer

    def get_queryset(self):
        user = self.request.user
        profile = getattr(user, 'profile', None)
        if profile and profile.empresa_id:
            # Parcelas de la empresa del usuario + sus parcelas individuales (si tuviera)
            return Plot.objects.filter(
                Q(zona__empresa=profile.empresa) | Q(owner=user)
            ).order_by('-created_at')
        # Usuario individual: solo sus propias parcelas
        return Plot.objects.filter(owner=user).order_by('-created_at')

    def perform_create(self, serializer):
        # Asegurar que el owner sea el usuario actual
        serializer.save(owner=self.request.user)


class InspectionViewSet(viewsets.ModelViewSet):
    """
    ViewSet para CRUD de Inspecciones.
    Cualquier usuario autenticado puede crear inspecciones en sus parcelas (o parcelas de su empresa).
    """
    permission_classes = [IsAuthenticated]  # cualquier usuario autenticado (ADMIN, WORKER, INDIVIDUAL)
    serializer_class = InspectionSerializer

    def get_queryset(self):
        user = self.request.user
        profile = getattr(user, 'profile', None)
        if profile and profile.empresa_id:
            # Inspecciones de parcelas de la empresa + inspecciones en sus parcelas individuales (si existieran)
            return Inspection.objects.filter(
                Q(plot__zona__empresa=profile.empresa) | Q(plot__owner=user)
            ).order_by('-inspected_at')
        # Individual: inspecciones de sus parcelas
        return Inspection.objects.filter(plot__owner=user).order_by('-inspected_at')

    def perform_create(self, serializer):
        # Asignar owner actual a la inspección
        serializer.save(owner=self.request.user)


class DiagnosticViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet de solo lectura para Diagnósticos de inspecciones.
    Los diagnósticos corresponden a inspecciones de parcelas accesibles por el usuario.
    """
    permission_classes = [IsAuthenticated]
    serializer_class = DiagnosticSerializer

    def get_queryset(self):
        user = self.request.user
        profile = getattr(user, 'profile', None)
        if profile and profile.empresa_id:
            return Diagnostic.objects.filter(
                Q(inspection__plot__zona__empresa=profile.empresa) | Q(inspection__plot__owner=user)
            ).order_by('-created_at')
        return Diagnostic.objects.filter(inspection__plot__owner=user).order_by('-created_at')


class ReportViewSet(viewsets.ModelViewSet):
    """
    ViewSet para CRUD de Reportes de datos.
    Cada usuario ve y crea sus propios reportes.
    """
    permission_classes = [IsAuthenticated]
    serializer_class = ReportSerializer

    def get_queryset(self):
        # Un usuario solo puede ver sus propios reportes
        return Report.objects.filter(owner=self.request.user).order_by('-created_at')

    def perform_create(self, serializer):
        # Asigna el propietario actual al reporte y lo guarda
        serializer.save(owner=self.request.user)
