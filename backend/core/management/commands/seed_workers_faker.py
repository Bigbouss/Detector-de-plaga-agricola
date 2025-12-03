# core/management/commands/seed_workers_faker.py
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from django.apps import apps
from django.contrib.auth import get_user_model
import random
from faker import Faker

User = get_user_model()

class Command(BaseCommand):
    help = "Seed workers using Faker and assign them to zones. Safe: skips existing emails."

    def add_arguments(self, parser):
        parser.add_argument('--company-tax', dest='company_tax', required=True,
                            help='tax_id de la empresa (ej: DEMO123)')
        parser.add_argument('--no-zone', dest='no_zone', type=int, default=4,
                            help='Cantidad de workers sin zona (por defecto 4)')
        parser.add_argument('--zone-a', dest='zone_a', type=int, default=4,
                            help='Cantidad de workers para Zona A (por defecto 4)')
        parser.add_argument('--zone-b', dest='zone_b', type=int, default=4,
                            help='Cantidad de workers para Zona B (por defecto 4)')
        parser.add_argument('--password', dest='password', default='WorkerDemo123!',
                            help='Password por defecto para los workers')

    @transaction.atomic
    def handle(self, *args, **options):
        faker = Faker()
        total = options['no_zone'] + options['zone_a'] + options['zone_b']
        company_tax = options['company_tax']
        password = options['password']

        # Resolver modelos
        try:
            Empresa = apps.get_model('emprises', 'Empresa')
            Role = apps.get_model('accounts', 'Role')
            Zona = apps.get_model('zonecrop', 'Zona')
            WorkerZoneAssignment = apps.get_model('zonecrop', 'WorkerZoneAssignment')
        except LookupError as e:
            raise CommandError(f"Error cargando modelos: {e}")

        empresa = Empresa.objects.filter(tax_id=company_tax).first()
        if not empresa:
            raise CommandError(f"Empresa con tax_id='{company_tax}' no encontrada.")

        role_worker = Role.objects.filter(code='WORKER').first()
        if not role_worker:
            raise CommandError("Role 'WORKER' no encontrado. Ejecuta seed_roles o crea el role.")

        # Obtener/crear zonas por nombre (case-insensitive)
        zona_a = Zona.objects.filter(nombre__iexact='Zona A', empresa=empresa).first()
        zona_b = Zona.objects.filter(nombre__iexact='Zona B', empresa=empresa).first()

        self.stdout.write(self.style.NOTICE(f"Empresa: {empresa.name} — crear {total} workers (A:{options['zone_a']} B:{options['zone_b']} NoZona:{options['no_zone']})"))
        created = []

        def make_unique_email(base):
            candidate = base
            idx = 0
            while User.objects.filter(email=candidate).exists():
                idx += 1
                candidate = f"{base.split('@')[0]}{idx}@{base.split('@')[1]}"
            return candidate

        # Helper para crear usuario
        def create_worker(fname, lname, email, phone):
            email = email.lower()
            if User.objects.filter(email=email).exists():
                self.stdout.write(self.style.WARNING(f"Salteando existente: {email}"))
                return None
            u = User.objects.create_user(
                email=email,
                password=password,
                first_name=fname,
                last_name=lname,
                phone=phone,
                role=role_worker,
                empresa=empresa
            )
            return u

        # 1) Crear no-zone
        for _ in range(options['no_zone']):
            fn = faker.first_name()
            ln = faker.last_name()
            base_email = f"{fn.lower()}.{ln.lower()}@{empresa.name.replace(' ','').lower()}.demo"
            email = make_unique_email(base_email)
            phone = f"+569{faker.random_number(digits=8, fix_len=True)}"
            u = create_worker(fn, ln, email, phone)
            if u:
                created.append(u)
                self.stdout.write(self.style.SUCCESS(f"Creado (no zona): {u.email}"))

        # 2) Crear Zona A
        if options['zone_a'] > 0 and not zona_a:
            self.stdout.write(self.style.WARNING("Zona A no encontrada. Los users destinados a Zona A serán creados sin asignación de zona."))
        for _ in range(options['zone_a']):
            fn = faker.first_name()
            ln = faker.last_name()
            base_email = f"{fn.lower()}.{ln.lower()}@{empresa.name.replace(' ','').lower()}.demo"
            email = make_unique_email(base_email)
            phone = f"+569{faker.random_number(digits=8, fix_len=True)}"
            u = create_worker(fn, ln, email, phone)
            if u:
                created.append(u)
                if zona_a:
                    WorkerZoneAssignment.objects.get_or_create(worker=u, zona=zona_a)
                    self.stdout.write(self.style.SUCCESS(f"Creado y asignado Zona A: {u.email} -> {zona_a.nombre}"))
                else:
                    self.stdout.write(self.style.SUCCESS(f"Creado (sin zona A encontrada): {u.email}"))

        # 3) Crear Zona B
        if options['zone_b'] > 0 and not zona_b:
            self.stdout.write(self.style.WARNING("Zona B no encontrada. Los users destinados a Zona B serán creados sin asignación de zona."))
        for _ in range(options['zone_b']):
            fn = faker.first_name()
            ln = faker.last_name()
            base_email = f"{fn.lower()}.{ln.lower()}@{empresa.name.replace(' ','').lower()}.demo"
            email = make_unique_email(base_email)
            phone = f"+569{faker.random_number(digits=8, fix_len=True)}"
            u = create_worker(fn, ln, email, phone)
            if u:
                created.append(u)
                if zona_b:
                    WorkerZoneAssignment.objects.get_or_create(worker=u, zona=zona_b)
                    self.stdout.write(self.style.SUCCESS(f"Creado y asignado Zona B: {u.email} -> {zona_b.nombre}"))
                else:
                    self.stdout.write(self.style.SUCCESS(f"Creado (sin zona B encontrada): {u.email}"))

        self.stdout.write(self.style.SUCCESS(f"Terminó. Creados: {len(created)} users."))
