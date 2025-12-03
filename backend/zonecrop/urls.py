from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ZonaViewSet, CultivoViewSet, WorkerZoneAssignmentViewSet

router = DefaultRouter()
router.register(r'zonas', ZonaViewSet, basename='zona')
router.register(r'cultivos', CultivoViewSet, basename='cultivo')
router.register(r'assignments', WorkerZoneAssignmentViewSet, basename='assignment')

urlpatterns = [
    path('', include(router.urls)),

]
