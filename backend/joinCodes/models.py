# joinCodes/models.py
from django.db import models, transaction
from django.conf import settings
from django.utils import timezone
from django.core.exceptions import ValidationError
import secrets
import string

# Usar la referencia en texto al modelo de usuario para evitar import cycles
UserRef = settings.AUTH_USER_MODEL


def default_code_expiry():
    # Expiración por defecto: 1 día
    return timezone.now() + timezone.timedelta(days=1)


class JoinCode(models.Model):
    class CodeRole(models.TextChoices):
        WORKER = "WORKER", "Trabajador"

    code = models.CharField(max_length=16, unique=True, editable=False)
    empresa = models.ForeignKey('emprises.Empresa', on_delete=models.CASCADE, related_name='join_codes')
    role = models.CharField(max_length=16, choices=CodeRole.choices, default=CodeRole.WORKER)
    created_by = models.ForeignKey(UserRef, on_delete=models.CASCADE, related_name='created_join_codes')
    max_uses = models.PositiveSmallIntegerField(default=1)
    used_count = models.PositiveSmallIntegerField(default=0)
    expires_at = models.DateTimeField(default=default_code_expiry)
    revoked = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        indexes = [models.Index(fields=['code'])]
        ordering = ['-created_at']

    def __str__(self):
        return f"{self.code} ({self.empresa.name})"

    @staticmethod
    def _generate_unique_code(length=8):
        alphabet = string.ascii_uppercase + string.digits
        # varios intentos para evitar colisiones en DB pequeñas
        for _ in range(20):
            code = ''.join(secrets.choice(alphabet) for _ in range(length))
            if not JoinCode.objects.filter(code=code).exists():
                return code
        # fallback — código más largo si hay colisiones
        return ''.join(secrets.choice(alphabet) for _ in range(length + 6))

    def save(self, *args, **kwargs):
        # asignar código si no fue provisto (permite manual override por serializer)
        if not self.code:
            self.code = self._generate_unique_code()
        super().save(*args, **kwargs)

    @property
    def is_valid(self):
        if self.revoked:
            return False
        if self.expires_at and timezone.now() > self.expires_at:
            return False
        return self.used_count < self.max_uses

    @transaction.atomic
    def redeem_for(self, user):
        """
        Redime el joincode para el `user`.
        - Si user.empresa es None: asigna empresa.
        - Si user.empresa == self.empresa: OK (idempotente).
        - Si user.empresa != self.empresa: ERROR.
        """
        # 1) validación empresa: solo fallar si pertenece a otra empresa distinta
        user_empresa_id = getattr(user, 'empresa_id', None)
        if user_empresa_id and user_empresa_id != self.empresa_id:
            raise ValidationError("El usuario ya pertenece a otra empresa distinta a la del joincode.")

        if not self.is_valid:
            raise ValidationError("Código inválido o expirado.")

        # Transacción para incrementar contador y crear uso atomically
        from django.db.models import F
        from django.apps import apps

        # Asegurarse de que Role esté disponible (import dinámico)
        Role = apps.get_model('accounts', 'Role')

        with transaction.atomic():
            # Asignar empresa al usuario si hace falta (si user.empresa_id es None)
            if not user_empresa_id:
                user.empresa = self.empresa

            # Asignar role si no corresponde
            try:
                target_role = Role.objects.get(code=self.role)
            except Role.DoesNotExist:
                # si falta la fila role, esto es un error serio de seed/migrations
                raise ValidationError("Role requerido por el joincode no existe en el sistema.")

            # Solo actualizar role si es distinto
            if not getattr(user, 'role_id', None) or getattr(getattr(user, 'role', None), 'code', None) != target_role.code:
                user.role = target_role

            # Guardar user con campos mínimos (empresa y role)
            # Usar update_fields para minimizar side-effects
            user.save(update_fields=['empresa', 'role'] if user_empresa_id is None else ['role'])

            # Incrementar used_count de forma segura
            JoinCode.objects.filter(pk=self.pk).update(used_count=F('used_count') + 1)
            # Crear JoinCodeUsage si no existía (evita UniqueConstraint fail)
            JoinCodeUsage = apps.get_model('joincodes', 'JoinCodeUsage')
            JoinCodeUsage.objects.get_or_create(join_code=self, user=user)

            # Refrescar campos de esta instancia para que caller vea used_count real
            self.refresh_from_db(fields=['used_count'])

        return user

    def clean(self):
        if self.expires_at and self.expires_at < timezone.now() + timezone.timedelta(minutes=15):
            raise ValidationError("La expiración mínima es de 15 minutos.")


class JoinCodeUsage(models.Model):
    join_code = models.ForeignKey(JoinCode, on_delete=models.CASCADE, related_name='usages')
    user = models.ForeignKey(UserRef, on_delete=models.CASCADE, related_name='joincode_usages')
    used_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('join_code', 'user')
        ordering = ['-used_at']

    def __str__(self):
        return f"{self.user} usó {self.join_code.code}"
