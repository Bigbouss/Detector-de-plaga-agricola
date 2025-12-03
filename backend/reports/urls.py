from rest_framework.routers import DefaultRouter
from django.urls import path, include
from .views import SessionReportViewSet, AggregatedReportViewSet

router = DefaultRouter()
router.register('session-reports', SessionReportViewSet, basename='sessionreport')
router.register('aggregated-reports', AggregatedReportViewSet, basename='aggregatedreport')

urlpatterns = [
    path('', include(router.urls)),
]
