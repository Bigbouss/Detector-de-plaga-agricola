"""
api/admin.py
Registro de modelos en el panel de administración para inspección de datos.
"""

from django.contrib import admin
from .models import Plot, Inspection, Diagnostic, UserProfile, Report


@admin.register(Plot)
class PlotAdmin(admin.ModelAdmin):
    list_display = ("name", "cultivo", "owner", "created_at")
    search_fields = ("name", "cultivo", "notes")
    list_filter = ("cultivo", "created_at")


@admin.register(Inspection)
class InspectionAdmin(admin.ModelAdmin):
    list_display = ("id", "plot", "owner", "inspected_at")
    search_fields = ("notes", "plot__name")
    list_filter = ("inspected_at",)


@admin.register(Diagnostic)
class DiagnosticAdmin(admin.ModelAdmin):
    list_display = ("label", "confidence", "inspection", "owner", "created_at")
    search_fields = ("label", "inspection__notes")
    list_filter = ("model_version",)


@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ("user", "display_name", "organization", "phone", "created_at")
    search_fields = ("display_name", "organization", "user__username")


@admin.register(Report)
class ReportAdmin(admin.ModelAdmin):
    list_display = ("title", "status", "format", "owner", "generated_at", "created_at")
    search_fields = ("title", "description")
    list_filter = ("status", "format", "created_at")
