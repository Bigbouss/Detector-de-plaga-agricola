from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import JoinCodeViewSet, JoinCompanyView, ValidateJoinCodeView

router = DefaultRouter()
router.register(r'joincodes', JoinCodeViewSet, basename='joincode')

urlpatterns = [
    path('', include(router.urls)),
    path('join-company/', JoinCompanyView.as_view(), name='join-company'),
    path('validate-code/', ValidateJoinCodeView.as_view(), name='validate-code'),
]
