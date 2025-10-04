# api/urls.py
from django.urls import path, include
from rest_framework.routers import DefaultRouter

# Vistas propias (API root, ping, registro, perfil y CRUD)
from .views import (
    ApiRootView,        # Raíz pública con enlaces a endpoints
    PingView,           # Comprobar JWT (requiere autenticación)
    RegisterView,       # Registro de usuarios (público)
    UserProfileMeView,  # Perfil del usuario autenticado (GET/PATCH/PUT)
    PlotViewSet,        # CRUD de parcelas
    InspectionViewSet,  # CRUD de inspecciones
    DiagnosticViewSet,  # CRUD de diagnósticos
    ReportViewSet,      # CRUD de reportes del historial
)

# Vistas de SimpleJWT (token create/refresh/verify)
from rest_framework_simplejwt.views import (
    TokenObtainPairView,   # /auth/jwt/create/
    TokenRefreshView,      # /auth/jwt/refresh/
    TokenVerifyView,       # /auth/jwt/verify/
)

# Router DRF para los ViewSets (genera /plots/, /inspections/, /diagnostics/, /reports/)
router = DefaultRouter()
router.register(r"plots", PlotViewSet, basename="plot")
router.register(r"inspections", InspectionViewSet, basename="inspection")
router.register(r"diagnostics", DiagnosticViewSet, basename="diagnostic")
router.register(r"reports", ReportViewSet, basename="report")

urlpatterns = [
    # Raíz y utilidades
    path("", ApiRootView.as_view(), name="api-root"),
    path("ping/", PingView.as_view(), name="api-ping"),
    path("auth/register/", RegisterView.as_view(), name="register"),

    # --- ENDPOINTS JWT ---
    path("auth/jwt/create/",  TokenObtainPairView.as_view(), name="jwt-create"),
    path("auth/jwt/refresh/", TokenRefreshView.as_view(),   name="jwt-refresh"),
    path("auth/jwt/verify/",  TokenVerifyView.as_view(),    name="jwt-verify"),

    # Perfil del usuario autenticado
    path("auth/profile/", UserProfileMeView.as_view(), name="profile"),
]


urlpatterns += router.urls
