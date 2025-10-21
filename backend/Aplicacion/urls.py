from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    # Endpoints de la API principal
    path('api/', include('api.urls')),
    # Endpoints de gestión de organizaciones (empresas, admins, workers)
    path('api/orgs/', include('cropcare_orgs.urls')),
]
