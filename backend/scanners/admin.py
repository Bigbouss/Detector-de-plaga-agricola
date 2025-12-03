# scanners/admin.py
from django.contrib import admin
from .models import ScannerSession, ScannerImage, ScannerResult, ModelVersion

@admin.register(ScannerSession)
class ScannerSessionAdmin(admin.ModelAdmin):
    list_display = ['session_id', 'worker_name', 'empresa', 'zona', 'status', 'started_at']
    list_filter = ['status', 'empresa', 'started_at']
    search_fields = ['session_id', 'worker_name']

@admin.register(ScannerResult)
class ScannerResultAdmin(admin.ModelAdmin):
    list_display = ['result_id', 'session', 'classification', 'confidence', 'has_plague']
    list_filter = ['has_plague', 'classification']