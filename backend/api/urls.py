"""
api/urls.py
URLs para la aplicación API principal de CropCare.
"""
from django.urls import path, include
from rest_framework.routers import DefaultRouter

# Vistas de la API (endpoints públicos y CRUD de recursos)
from .views import (
    ApiRootView,        # Raíz pública de la API
    PingView,           # Verificación de autenticación JWT
    RegisterView,       # Registro de usuarios individuales
    UserProfileMeView,  # Obtener/actualizar perfil personal del usuario
    PlotViewSet,        # CRUD de parcelas (plots)
    InspectionViewSet,  # CRUD de inspecciones
    DiagnosticViewSet,  # CRUD de diagnósticos
    ReportViewSet,      # CRUD de reportes
    ZonaViewSet,        # CRUD de zonas
)

# Vistas de SimpleJWT (crear/actualizar/verificar tokens)
from rest_framework_simplejwt.views import (
    TokenObtainPairView,   # /auth/jwt/create/
    TokenRefreshView,      # /auth/jwt/refresh/
    TokenVerifyView,       # /auth/jwt/verify/
)

# Router DRF para los ViewSets principales (zonas, plots, inspections, diagnostics, reports)
router = DefaultRouter()
router.register(r'zonas', ZonaViewSet, basename='zonas')
router.register(r'plots', PlotViewSet, basename='plot')
router.register(r'inspections', InspectionViewSet, basename='inspection')
router.register(r'diagnostics', DiagnosticViewSet, basename='diagnostic')
router.register(r'reports', ReportViewSet, basename='report')

urlpatterns = [
    # Rutas raíz y utilidades
    path("", ApiRootView.as_view(), name="api-root"),
    path("ping/", PingView.as_view(), name="api-ping"),
    path("auth/register/", RegisterView.as_view(), name="register"),
    # Endpoints JWT para autenticación
    path("auth/jwt/create/",  TokenObtainPairView.as_view(), name="jwt-create"),
    path("auth/jwt/refresh/", TokenRefreshView.as_view(),   name="jwt-refresh"),
    path("auth/jwt/verify/",  TokenVerifyView.as_view(),    name="jwt-verify"),
    # Perfil del usuario autenticado (lectura/actualización)
    path("auth/profile/", UserProfileMeView.as_view(), name="profile"),
]

urlpatterns += router.urls