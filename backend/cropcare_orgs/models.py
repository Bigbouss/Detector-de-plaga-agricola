"""
cropcare_orgs/models.py
Modelos para gestionar empresas, perfiles de empleados y códigos de invitación (JoinCode).
"""
from django.conf import settings
from django.core.exceptions import ValidationError
from django.db import models, transaction
from django.utils import timezone
import secrets
import string

User = settings.AUTH_USER_MODEL


# Función para obtener la fecha/hora de expiración por defecto (1 día desde ahora)
def default_code_expiry():
    return timezone.now() + timezone.timedelta(days=1)


class Empresa(models.Model):
    name = models.CharField(max_length=160)
    legal_name = models.CharField(max_length=200, blank=True)
    tax_id = models.CharField(max_length=40, blank=True)  # RUT u otro
    country = models.CharField(max_length=2, default='CL')
    timezone = models.CharField(max_length=64, default='America/Santiago')
    owner = models.ForeignKey(User, on_delete=models.PROTECT, related_name='owned_companies')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Empresa'
        verbose_name_plural = 'Empresas'
        indexes = [models.Index(fields=['name'])]

    def __str__(self) -> str:
        return self.name


class EmployeeProfile(models.Model):
    class Role(models.TextChoices):
        ADMIN = 'ADMIN', 'Admin'
        WORKER = 'WORKER', 'Trabajador'
        INDIVIDUAL = 'INDIVIDUAL', 'Individual'

    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='profile')
    empresa = models.ForeignKey(Empresa, null=True, blank=True, on_delete=models.CASCADE, related_name='members')
    role = models.CharField(max_length=16, choices=Role.choices, default=Role.INDIVIDUAL)
    is_active = models.BooleanField(default=True)
    can_manage_plots = models.BooleanField(default=False)  # True si WORKER puede crear/editar parcelas/zonas
    joined_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Perfil de Empleado'
        verbose_name_plural = 'Perfiles de Empleado'

    def clean(self):
        # Si es ADMIN o WORKER, debe tener empresa asignada
        if self.role in {self.Role.ADMIN, self.Role.WORKER} and self.empresa is None:
            raise ValidationError('Los perfiles ADMIN/WORKER deben pertenecer a una empresa.')
        # Si es INDIVIDUAL, no debe tener empresa
        if self.role == self.Role.INDIVIDUAL and self.empresa is not None:
            raise ValidationError('Los perfiles INDIVIDUAL no deben pertenecer a una empresa.')

    def __str__(self) -> str:
        return f"{self.user} ({self.role})"


class JoinCode(models.Model):
    class CodeRole(models.TextChoices):
        WORKER = 'WORKER', 'Trabajador'

    code = models.CharField(max_length=16, unique=True)
    empresa = models.ForeignKey(Empresa, on_delete=models.CASCADE, related_name='join_codes')
    role = models.CharField(max_length=16, choices=CodeRole.choices, default=CodeRole.WORKER)
    created_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_join_codes')
    max_uses = models.PositiveSmallIntegerField(default=1)
    used_count = models.PositiveSmallIntegerField(default=0)
    expires_at = models.DateTimeField(null=True, blank=True, default=default_code_expiry)
    revoked = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        indexes = [models.Index(fields=['code'])]

    def __str__(self) -> str:
        return f"{self.code} -> {self.empresa.name} ({self.role})"

    @staticmethod
    def _generate_unique_code(length: int = 8) -> str:
        alphabet = string.ascii_uppercase + string.digits
        for _ in range(10):
            candidate = ''.join(secrets.choice(alphabet) for _ in range(length))
            if not JoinCode.objects.filter(code=candidate).exists():
                return candidate
        # último intento con un código más largo si hay colisiones repetidas
        return ''.join(secrets.choice(alphabet) for _ in range(length + 4))

    def save(self, *args, **kwargs):
        # Generar un código aleatorio único al crear
        if not self.code:
            self.code = self._generate_unique_code()
        return super().save(*args, **kwargs)

    @property
    def is_valid(self) -> bool:
        # Un código es válido si no está revocado, no expiró y no alcanzó max_uses
        if self.revoked:
            return False
        if self.expires_at and timezone.now() > self.expires_at:
            return False
        if self.used_count >= self.max_uses:
            return False
        return True

    @transaction.atomic
    def redeem_for(self, user) -> EmployeeProfile:
        """
        Redime este código para unirse a la empresa, asignando al usuario como WORKER.
        Retorna el EmployeeProfile resultante.
        """
        # No permitir si usuario ya está en una empresa
        profile = getattr(user, 'profile', None)
        if profile and profile.empresa_id:
            raise ValidationError('El usuario ya pertenece a una empresa.')
        if not self.is_valid:
            raise ValidationError('Código inválido o expirado.')
        # Crear o actualizar perfil como WORKER de la empresa
        profile, _ = EmployeeProfile.objects.get_or_create(user=user)
        profile.role = EmployeeProfile.Role.WORKER
        profile.empresa = self.empresa
        profile.can_manage_plots = False  # por defecto, los nuevos trabajadores no pueden gestionar parcelas
        profile.full_clean()
        profile.save()
        # Incrementar contador de usos
        self.used_count = models.F('used_count') + 1
        self.save(update_fields=['used_count'])
        # Refrescar el valor actualizado de used_count desde la BD
        self.refresh_from_db(fields=['used_count'])
        return profile

    def clean(self):
        # Validar que expires_at sea al menos 15 minutos en el futuro
        if self.expires_at and self.expires_at < timezone.now() + timezone.timedelta(minutes=15):
            raise ValidationError('El tiempo de expiración mínimo para un JoinCode es de 15 minutos.')
