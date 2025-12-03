from django.contrib import admin
from .models import Empresa

@admin.register(Empresa)
class EmpresaAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'tax_id', 'owner', 'country', 'created_at')
    search_fields = ('name', 'tax_id', 'legal_name')
    list_filter = ('country',)
