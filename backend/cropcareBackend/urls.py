from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/accounts/', include('accounts.urls')),
    path('api/emprises/', include('emprises.urls')),
    path('api/joincodes/', include('joinCodes.urls')),
    path('api/zonecrop/', include('zonecrop.urls')),
    path('api/scanners/', include('scanners.urls')),
    path('api/reports/', include('reports.urls')),
]

# AGREGAR: Servir archivos media en desarrollo
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)