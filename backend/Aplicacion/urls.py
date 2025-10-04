# Aplicacion/urls.py (resumen)
from django.contrib import admin
from django.urls import path, include
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView
from django.conf import settings
from django.conf.urls.static import static
from django.http import JsonResponse

def status_view(_request):
    return JsonResponse({"status": "ok", "service": "cropcare-backend"})

urlpatterns = [
    path("admin/", admin.site.urls),
    path("status/", status_view, name="status"),
    path("api/schema/", SpectacularAPIView.as_view(), name="api-schema"),
    path("api/docs/", SpectacularSwaggerView.as_view(url_name="api-schema"), name="api-docs"),
    path("api/v1/cropcare/", include("api.urls")),  # 👈 aquí vive toda la API
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
