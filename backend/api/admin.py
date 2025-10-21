"""
api/admin.py
Registro de modelos en el panel de administración para inspección de datos.
"""
from django.contrib import admin
from django.core.exceptions import ValidationError  # usamos esta para bloquear guardado
from .models import Cultivo, Zona, Plot, Inspection, Diagnostic, UserProfile, Report

@admin.register(Cultivo)
class CultivoAdmin(admin.ModelAdmin):
    list_display = ('id', 'nombre', 'created_at')
    search_fields = ('nombre',)

@admin.register(Zona)
class ZonaAdmin(admin.ModelAdmin):
    list_display = ('id', 'nombre', 'cultivo', 'empresa', 'created_at')
    search_fields = ('nombre', 'cultivo__nombre', 'empresa__name')
    list_filter = ('empresa',)

@admin.register(Plot)
class PlotAdmin(admin.ModelAdmin):
    list_display = ("id", "name", "cultivo", "owner", "created_at")
    search_fields = ("name", "cultivo__nombre", "notes")
    list_filter = ("cultivo", "created_at")

@admin.register(Inspection)
class InspectionAdmin(admin.ModelAdmin):
    """
    ÚNICA clase para administrar Inspections:
    - define el listado
    - valida que en la creación haya imagen
    """
    list_display = ("id", "plot", "owner", "inspected_at")
    search_fields = ("notes", "plot__name")
    list_filter = ("inspected_at",)

    def save_model(self, request, obj, form, change):
        # Exigir imagen al CREAR (no al editar)
        if not change and not obj.image:
            # Levantamos ValidationError para que el admin muestre el error y no guarde
            raise ValidationError("La imagen es obligatoria para crear una inspección.")
        super().save_model(request, obj, form, change)

@admin.register(Diagnostic)
class DiagnosticAdmin(admin.ModelAdmin):
    list_display = ("id", "label", "confidence", "inspection", "owner", "created_at")
    search_fields = ("label", "inspection__notes")
    list_filter = ("model_version",)

@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ("user", "display_name", "organization", "phone", "created_at")
    search_fields = ("display_name", "organization", "user__username")

@admin.register(Report)
class ReportAdmin(admin.ModelAdmin):
    list_display = ("id", "title", "status", "format", "owner", "generated_at", "created_at")
    search_fields = ("title", "description")
    list_filter = ("status", "format", "created_at")
