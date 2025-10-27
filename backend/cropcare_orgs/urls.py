"""
cropcare_orgs/urls.py
URLs para la aplicación de organizaciones (empresas, registro de admins/workers, códigos de invitación).
"""
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenRefreshView,
    TokenVerifyView,
)
from .views import (
    RegisterAdminView,
    RegisterWorkerView,
    JoinCodeViewSet,
    CompanyWorkersViewSet,
    MeView,
    JoinCompanyView,  # <-- añadido
    ValidateJoinCodeView,
)

router = DefaultRouter()
router.register(r'join-codes', JoinCodeViewSet, basename='join-codes')
router.register(r'workers', CompanyWorkersViewSet, basename='workers')

urlpatterns = [
    # Autenticación JWT (login común para admin y worker)
    path('auth/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('auth/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('auth/token/verify/', TokenVerifyView.as_view(), name='token_verify'),
    # Registro de administradores y trabajadores
    path('auth/register-admin/', RegisterAdminView.as_view(), name='register_admin'),
    path('auth/register-worker/', RegisterWorkerView.as_view(), name='register_worker'),
    # Perfil propio del usuario (admin/worker)
    path('auth/me/', MeView.as_view(), name='me'),
    # Unirse a empresa con código (usuario existente)
    path('auth/join-company/', JoinCompanyView.as_view(), name='join-company'),
    path('join-codes/validate/', ValidateJoinCodeView.as_view(), name='validate_joincode'),
    # Recursos de empresa (códigos y lista/creación de trabajadores)
    path('', include(router.urls)),
]
