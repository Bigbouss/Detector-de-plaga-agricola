from django.contrib import admin
from .models import JoinCode, JoinCodeUsage

@admin.register(JoinCode)
class JoinCodeAdmin(admin.ModelAdmin):
    list_display = ('code', 'empresa', 'role', 'max_uses', 'used_count', 'revoked', 'expires_at', 'created_at')
    search_fields = ('code', 'empresa__name')
    list_filter = ('revoked', 'role')

@admin.register(JoinCodeUsage)
class JoinCodeUsageAdmin(admin.ModelAdmin):
    list_display = ('join_code', 'user', 'used_at')
    search_fields = ('join_code__code', 'user__email')
