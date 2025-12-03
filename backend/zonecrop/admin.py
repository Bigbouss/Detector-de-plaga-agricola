from django.contrib import admin
from .models import Zona, Cultivo, WorkerZoneAssignment

@admin.register(Zona)
class ZonaAdmin(admin.ModelAdmin):
    list_display = ('id', 'nombre', 'empresa', 'created_at')
    search_fields = ('nombre', 'empresa__name')
    list_filter = ('empresa',)

@admin.register(Cultivo)
class CultivoAdmin(admin.ModelAdmin):
    list_display = ('id', 'nombre', 'zona', 'created_at')
    search_fields = ('nombre', 'zona__nombre')
    list_filter = ('zona',)

@admin.register(WorkerZoneAssignment)
class WorkerZoneAssignmentAdmin(admin.ModelAdmin):
    list_display = ('id', 'worker', 'zona', 'assigned_at')
    search_fields = ('worker__email', 'zona__nombre')
