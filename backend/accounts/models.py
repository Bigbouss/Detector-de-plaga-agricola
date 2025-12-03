# accounts/models.py
from django.db import models
from django.contrib.auth.models import (
    AbstractBaseUser, PermissionsMixin, BaseUserManager
)
from django.core.validators import RegexValidator
from django.core.exceptions import ValidationError
from django.apps import apps

class TimeStampedModel(models.Model):
    created_at = models.DateTimeField(auto_now_add=True, null=False)
    updated_at = models.DateTimeField(auto_now=True, null=False)

    class Meta:
        abstract = True


class Role(models.Model):
    """
    Roles únicos en el sistema: 'ADMIN' y 'WORKER'.
    Solo se crean estas filas (seeding en signals).
    """
    code = models.CharField(max_length=32, unique=True, null=False)
    name = models.CharField(max_length=100, null=False)

    class Meta:
        db_table = "accounts_role"
        verbose_name = "Role"
        verbose_name_plural = "Roles"

    def __str__(self):
        return self.code


class UserManager(BaseUserManager):
    use_in_migrations = True

    def _create_user(self, email, password, **extra_fields):
        if not email:
            raise ValueError("El email es obligatorio.")
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        if password:
            user.set_password(password)
        else:
            user.set_unusable_password()
        user.save(using=self._db)
        return user

    def create_user(self, email, password=None, **extra_fields):
        extra_fields.setdefault('is_active', True)
        return self._create_user(email, password, **extra_fields)

    def create_superuser(self, email, password=None, **extra_fields):
        """
        Crea un superuser Django compatible con `createsuperuser`.
        Intenta asignar role=ADMIN si la fila existe; si no, permite crear superuser global.
        """
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)

        # Intentar asignar role ADMIN automáticamente si existe (no fallar si no existe)
        try:
            RoleModel = apps.get_model('accounts', 'Role')
            admin_role = RoleModel.objects.filter(code='ADMIN').first()
            if admin_role:
                extra_fields.setdefault('role', admin_role)
        except Exception:
            # no rompemos la creación de superuser por un erro al buscar Role
            pass

        # Crea el usuario (no forzamos empresa aquí; superuser puede ser global)
        return self._create_user(email, password, **extra_fields)


class User(AbstractBaseUser, PermissionsMixin, TimeStampedModel):
    """
    Modelo único de usuario (admin y worker). No duplicamos empresa ni roles aquí.
    Reglas importantes:
      - role: FK a Role ('ADMIN'/'WORKER')
      - empresa: FK a emprises.Empresa (no null para producción, pero se permite temporalmente
                 en creación ADMIN antes de crear la empresa; ese caso se maneja atomicamente)
      - un usuario no puede cambiar de empresa una vez asignada.
    """
    email = models.EmailField(unique=True, null=False)
    first_name = models.CharField(max_length=120, null=False)
    last_name = models.CharField(max_length=120, null=True, blank=True, default="prueba")
    phone = models.CharField(
        max_length=30, null=False,
        validators=[RegexValidator(r'^[\d+\-\s\(\)]+$', message="Teléfono inválido")]
    )

    # FK a Role (tabla separada pero única)
    role = models.ForeignKey('accounts.Role', on_delete=models.PROTECT, null=False, related_name='users')

    # FK a Empresa (app emprises). Se recomienda NOT NULL en esquema final,
    # pero aquí permitimos null en un flujo controlado (registro admin creación empresa).
    empresa = models.ForeignKey('emprises.Empresa', on_delete=models.PROTECT, null=True, blank=True, related_name='members')

    # Flags Django
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)   # usado para admin site
    is_superuser = models.BooleanField(default=False)

    objects = UserManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['first_name', 'phone']

    class Meta:
        db_table = 'accounts_user'
        verbose_name = 'User'
        verbose_name_plural = 'Users'
        indexes = [
            models.Index(fields=['email']),
            models.Index(fields=['role']),
        ]

    def __str__(self):
        code = self.role.code if self.role_id else "NO-ROLE"
        return f"{self.email} ({code})"

    def clean(self):
        # normalizar email
        if self.email:
            self.email = self.email.lower().strip()

        # Si es superuser global, permitimos bypass de role/empresa validaciones
        if getattr(self, 'is_superuser', False):
            # todavía normalizamos email y terminamos (superuser puede ser global)
            return

        # role debe ser ADMIN o WORKER (solo se evalúa para usuarios normales)
        if not self.role or getattr(getattr(self, 'role', None), 'code', None) not in ('ADMIN', 'WORKER'):
            raise ValidationError("Role inválido: debe ser 'ADMIN' o 'WORKER'.")

        # evita cambio de empresa una vez establecido
        if self.pk:
            prev = User.objects.filter(pk=self.pk).only('empresa_id').first()
            if prev and prev.empresa_id and self.empresa_id != prev.empresa_id:
                raise ValidationError("No puedes cambiar la empresa de un usuario ya asignado.")

        # Todos deben pertenecer a una empresa EXCEPTO:
        # - ADMIN recién creado (role ADMIN y sin pk)
        if not self.empresa_id:
            if not (self.role and getattr(self.role, 'code', None) == 'ADMIN' and not self.pk):
                raise ValidationError("Todos los usuarios deben pertenecer a una empresa.")

        # ADMIN debe ser is_staff
        if self.role and self.role.code == 'ADMIN' and not self.is_staff:
            raise ValidationError("Usuarios ADMIN deben tener is_staff=True.")

    def save(self, *args, **kwargs):
        # forzar validaciones de clean() antes de guardar
        self.full_clean()
        super().save(*args, **kwargs)
