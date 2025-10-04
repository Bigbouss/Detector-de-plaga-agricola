# api/views.py
"""
ViewSets con corte por 'owner' + endpoints auxiliares:
- ApiRootView: raíz informativa
- PingView: protegido, para validar JWT
- RegisterView: crear usuario (público)
- UserProfileMeView: obtener/actualizar el perfil del usuario autenticado
- ViewSets: Plot, Inspection, Diagnostic, Report
"""

from rest_framework import viewsets, permissions, filters, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.generics import RetrieveUpdateAPIView
from django_filters.rest_framework import DjangoFilterBackend

from .models import Plot, Inspection, Diagnostic, Report, UserProfile
from .serializers import (
    PlotSerializer, InspectionSerializer, DiagnosticSerializer,
    ReportSerializer, UserRegisterSerializer, UserProfileSerializer
)


# ---- Mixin: siempre filtra por owner ----
class OwnerQuerysetMixin:
    def get_queryset(self):
        """
        Cada ViewSet define self.queryset (todos los objetos).
        Aquí *cortamos* por el usuario autenticado para que cada
        persona solo vea/edite lo suyo.
        """
        return self.queryset.filter(owner=self.request.user)

    def perform_create(self, serializer):
        """
        'owner' lo setea HiddenField(CurrentUserDefault()) en los serializers,
        por lo que simplemente guardamos.
        """
        serializer.save()


# ---- API Root y Ping ----
class ApiRootView(APIView):
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        # build_absolute_uri te permite construir URLs absolutas
        base = request.build_absolute_uri  # función
        return Response({
            "name": "cropcare-api",
            "version": "v1",
            "endpoints": {
                "ping":        base("ping/"),
                "jwt_create":  base("auth/jwt/create/"),
                "jwt_refresh": base("auth/jwt/refresh/"),
                "jwt_verify":  base("auth/jwt/verify/"),
                "register":    base("auth/register/"),
                "profile":     base("auth/profile/"),
                "docs": "/api/docs/",
            }
        })


class PingView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        return Response({"message": "pong", "user": request.user.username})


class RegisterView(APIView):
    """
    Registro simple. Tras crear, el cliente debe obtener JWT en /auth/jwt/create/.
    """
    permission_classes = [permissions.AllowAny]

    def post(self, request):
        serializer = UserRegisterSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        return Response(
            {"id": user.id, "username": user.username, "email": user.email},
            status=status.HTTP_201_CREATED
        )


class UserProfileMeView(RetrieveUpdateAPIView):
    """
    Devuelve y permite actualizar el perfil del usuario autenticado.
    - GET  /auth/profile/       → ver perfil
    - PUT/PATCH /auth/profile/  → actualizar display_name, organization, phone
    """
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = UserProfileSerializer

    def get_object(self):
        # Crea el perfil si no existe (conveniente para front)
        profile, _ = UserProfile.objects.get_or_create(user=self.request.user)
        return profile


# ---- CRUD protegidos por JWT + owner ----
class PlotViewSet(OwnerQuerysetMixin, viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = PlotSerializer
    queryset = Plot.objects.all()

    # Filtros, búsqueda y ordenación
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ["cultivo", "fecha_siembra"]
    search_fields = ["name", "cultivo", "notes"]
    ordering_fields = ["created_at", "name", "fecha_siembra"]
    ordering = ["-created_at"]


class InspectionViewSet(OwnerQuerysetMixin, viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = InspectionSerializer
    queryset = Inspection.objects.select_related("plot").all()

    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ["plot", "inspected_at"]
    search_fields = ["notes", "plot__name", "plot__cultivo"]
    ordering_fields = ["inspected_at", "created_at"]
    ordering = ["-inspected_at"]


class DiagnosticViewSet(OwnerQuerysetMixin, viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = DiagnosticSerializer
    queryset = Diagnostic.objects.select_related("inspection", "inspection__plot").all()

    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ["inspection", "label"]
    search_fields = ["label", "inspection__notes", "inspection__plot__name"]
    ordering_fields = ["created_at", "confidence"]
    ordering = ["-created_at"]


class ReportViewSet(OwnerQuerysetMixin, viewsets.ModelViewSet):
    """
    Módulo de historial de reportes:
    - Creas la entidad Report y, en un proceso aparte, generas el archivo,
      marcas status='ready' y adjuntas file si corresponde.
    """
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = ReportSerializer
    queryset = Report.objects.all()

    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ["status", "format"]
    search_fields = ["title", "description"]
    ordering_fields = ["created_at", "generated_at"]
    ordering = ["-created_at"]
