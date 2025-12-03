from rest_framework.routers import DefaultRouter
from django.urls import path, include
from .views import (
    ModelVersionViewSet, ScannerSessionViewSet,
    ScannerImageViewSet, ScannerResultViewSet
)

router = DefaultRouter()
router.register('models', ModelVersionViewSet, basename='modelversion')
router.register('sessions', ScannerSessionViewSet, basename='scannersession')
router.register('images', ScannerImageViewSet, basename='scannerimage')
router.register('results', ScannerResultViewSet, basename='scannerresult')

urlpatterns = [
    path('', include(router.urls)),
]