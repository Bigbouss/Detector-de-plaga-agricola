"""
cropcare_orgs/admin.py
Registro de modelos de organizaciones en el panel de administración.
"""
from django.contrib import admin
from .models import Empresa, EmployeeProfile, JoinCode

@admin.register(Empresa)
class EmpresaAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'owner', 'country', 'created_at')
    search_fields = ('name', 'owner__username', 'owner__email')

@admin.register(EmployeeProfile)
class EmployeeProfileAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'role', 'empresa', 'is_active', 'can_manage_plots', 'joined_at')
    search_fields = ('user__username', 'user__email', 'empresa__name')
    list_filter = ('role', 'is_active', 'can_manage_plots')

@admin.register(JoinCode)
class JoinCodeAdmin(admin.ModelAdmin):
    list_display = ('id', 'code', 'empresa', 'role', 'max_uses', 'used_count', 'expires_at', 'revoked')
    search_fields = ('code', 'empresa__name')
    list_filter = ('role', 'revoked')
