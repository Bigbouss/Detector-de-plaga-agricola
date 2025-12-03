from rest_framework.routers import DefaultRouter
from django.urls import path, include
from .views import EmpresaViewSet, WorkerViewSet

router = DefaultRouter()
router.register('empresas', EmpresaViewSet, basename='empresa')
router.register(r'workers', WorkerViewSet, basename='worker') #nuevo

urlpatterns = [
    path('', include(router.urls)),
]
