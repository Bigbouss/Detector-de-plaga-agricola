from django.db import models
from django.conf import settings
from django.core.exceptions import ValidationError
from django.core.validators import RegexValidator

class TimeStampedModel(models.Model):
    created_at = models.DateTimeField(auto_now_add=True, null=False)
    updated_at = models.DateTimeField(auto_now=True, null=False)

    class Meta:
        abstract = True

class Empresa(TimeStampedModel):
    """
    Empresa: campos obligatorios (no-null)
    tax_id: RUT u otro identificador fiscal (string, máximo 12)
    name: nombre comercial (max 200)
    legal_name: nombre legal de la empresa (max 160)
    country: código ISO2 (ej. 'CL') - exactamente 2 caracteres
    owner: usuario que creó la empresa; debe ser ADMIN (validación en clean)
    """
    tax_id = models.CharField(max_length=12, null=False, blank=False, unique=True, db_index=True)
    name = models.CharField(max_length=200, null=False, blank=False)
    legal_name = models.CharField(max_length=160, null=False, blank=False)
    country = models.CharField(max_length=2, null=False, blank=False,
                               validators=[RegexValidator(r'^[A-Z]{2}$', 'Debe ser código ISO2 en mayúsculas')])

    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL, 
        on_delete=models.PROTECT, 
        related_name='owned_emprises',
        null=True,
        blank=True
    )

    class Meta:
        db_table = 'emprises_empresa'
        verbose_name = 'Empresa'
        verbose_name_plural = 'Empresas'
        indexes = [
            models.Index(fields=['tax_id']),
            models.Index(fields=['name']),
        ]

    def __str__(self):
        return f"{self.name} ({self.tax_id})"

    def clean(self):
        # campos normalizados
        if self.tax_id:
            self.tax_id = self.tax_id.strip().upper()
        if self.country:
            self.country = self.country.strip().upper()

        # validaciones básicas
        if not self.tax_id or not self.name or not self.legal_name or not self.country:
            raise ValidationError("tax_id, name, legal_name y country son obligatorios.")

        if len(self.tax_id) > 12:
            raise ValidationError("tax_id no puede exceder 12 caracteres.")

        owner = getattr(self, 'owner', None)
        if owner is not None:
            # Solo validar role si el owner está asignado
            role = getattr(owner, 'role', None)
            if role:
                code = getattr(role, 'code', None)
                if code != 'ADMIN':
                    raise ValidationError("El owner de la empresa debe tener role = 'ADMIN'.")

    def save(self, *args, **kwargs):
        self.full_clean()
        super().save(*args, **kwargs)