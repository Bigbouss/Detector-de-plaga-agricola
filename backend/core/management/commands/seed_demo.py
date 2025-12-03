# core/management/commands/seed_demo.py
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from django.utils import timezone
from django.apps import apps
from django.contrib.auth import get_user_model
import random, string, secrets

User = get_user_model()

def _random_code(length=8):
    alphabet = string.ascii_uppercase + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length))

def safe_get_model(possible_app_labels, model_name):
    last_exc = None
    for label in possible_app_labels:
        try:
            return apps.get_model(label, model_name)
        except (LookupError, Exception) as e:
            last_exc = e
    raise LookupError(f"No model {model_name} found in any of {possible_app_labels}") from last_exc

class Seeder:
    def __init__(self):
        self.Role = safe_get_model(['accounts'], 'Role')
        self.Empresa = safe_get_model(['emprises'], 'Empresa')

        try:
            self.JoinCode = safe_get_model(['joinCodes','joincodes'], 'JoinCode')
        except LookupError:
            self.JoinCode = None

        try:
            self.Zona = safe_get_model(['zoneCrop','zonecrop','zone_crop'], 'Zona')
            self.Cultivo = safe_get_model(['zoneCrop','zonecrop','zone_crop'], 'Cultivo')
            self.WorkerZoneAssignment = safe_get_model(['zoneCrop','zonecrop','zone_crop'], 'WorkerZoneAssignment')
        except LookupError:
            self.Zona = None
            self.Cultivo = None
            self.WorkerZoneAssignment = None

        try:
            self.ModelVersion = safe_get_model(['scanners','Scanners'], 'ModelVersion')
        except LookupError:
            self.ModelVersion = None

        self.now = timezone.now()

    @transaction.atomic
    def seed_roles(self):
        created = []
        for code, name in (('ADMIN','Administrador'), ('WORKER','Trabajador')):
            obj, created_flag = self.Role.objects.get_or_create(code=code, defaults={'name': name})
            if created_flag:
                created.append(obj)
        return created

    @transaction.atomic
    def create_admin_and_company(self, admin_email='admin@example.com', company_tax='DEMO0001', company_name='Empresa Demo', password='DemoPass123!'):
        role_admin = self.Role.objects.get(code='ADMIN')
        admin_user = User.objects.create_user(
            email=admin_email,
            password=password,
            first_name='Admin',
            last_name='Demo',
            phone='+56900000000',
            role=role_admin,
            empresa=None,
            is_staff=True
        )
        empresa = self.Empresa.objects.create(
            tax_id=company_tax,
            name=company_name,
            legal_name=f"{company_name} S.A.",
            country='CL',
            owner=admin_user
        )
        admin_user.empresa = empresa
        admin_user.is_superuser = True
        admin_user.save()
        return admin_user, empresa

    @transaction.atomic
    def seed_zones_and_crops(self, empresa, zone_names=None):
        """
        Crea las zonas indicadas y tres cultivos básicos por zona si zone_names provisto.
        Si zone_names es None -> NO hace nada (evita creación automática no deseada).
        """
        if not self.Zona or not self.Cultivo:
            return [], []
        if zone_names is None:
            return [], []
        zonas = []
        cultivos = []
        for zname in zone_names:
            zname_clean = zname.strip()
            zona, _ = self.Zona.objects.get_or_create(nombre=zname_clean, empresa=empresa,
                                                      defaults={'descripcion': f"Zona demo {zname_clean}"})
            zonas.append(zona)
            # cultivos demo — puedes personalizar la lista si quieres
            for crop in ['Papas', 'Zanahoria', 'Tomate']:
                cult_name = f"{crop} {zname_clean}"
                cult, _ = self.Cultivo.objects.get_or_create(nombre=cult_name, zona=zona)
                cultivos.append(cult)
        return zonas, cultivos

    @transaction.atomic
    def seed_workers(self, empresa, count=5, password='WorkerPass123!'):
        role_worker = self.Role.objects.get(code='WORKER')
        created_workers = []
        start = self.UserCountOffset(empresa)
        for i in range(count):
            email = f"worker{start + i}@{empresa.name.replace(' ','').lower()}.demo"
            if User.objects.filter(email=email).exists():
                continue
            user = User.objects.create_user(
                email=email,
                password=password,
                first_name=f'Worker{start + i}',
                last_name='Demo',
                phone=f'+5691000{100+i}',
                role=role_worker,
                empresa=empresa
            )
            created_workers.append(user)
        return created_workers

    def UserCountOffset(self, empresa):
        cnt = User.objects.filter(empresa=empresa, email__icontains=empresa.name.replace(' ','').lower()).count()
        return cnt + 1

    @transaction.atomic
    def create_join_codes(self, empresa, created_by_user, count=3, max_uses=1):
        if not self.JoinCode:
            return []
        codes = []
        for _ in range(count):
            kwargs = {
                'code': _random_code(8),
                'empresa': empresa,
                'created_by': created_by_user,
                'max_uses': max_uses,
                'used_count': 0,
                'expires_at': self.now + timezone.timedelta(days=7),
                'revoked': False
            }
            allowed = {f.name for f in self.JoinCode._meta.get_fields()}
            safe_kwargs = {k:v for k,v in kwargs.items() if k in allowed}
            jc = self.JoinCode.objects.create(**safe_kwargs)
            codes.append(jc)
        return codes

    @transaction.atomic
    def assign_workers_to_zones(self, workers, zonas):
        if not self.WorkerZoneAssignment or not zonas:
            return []
        created = []
        for w in workers:
            z = random.choice(zonas)
            obj, created_flag = self.WorkerZoneAssignment.objects.get_or_create(worker=w, zona=z)
            created.append(obj)
        return created

    def bulk_seed_demo(self, admin_email='admin@example.com', company_tax='DEMO0001', company_name='Empresa Demo', workers_count=5, create_joincodes=False, create_zones=False, zone_names=None):
        """
        Ahora: crear zonas es opcional (create_zones flag). Si create_zones==True y zone_names provisto,
        se crearán las zonas dadas; si create_zones==True y zone_names is None, se crea el set por defecto.
        """
        self.seed_roles()
        admin, empresa = self.create_admin_and_company(admin_email=admin_email, company_tax=company_tax, company_name=company_name)
        zonas, cultivos = ([], [])
        if create_zones:
            if zone_names is None:
                zone_names = ['Zona Norte','Zona Sur','Zona Este']
            zonas, cultivos = self.seed_zones_and_crops(empresa, zone_names=zone_names)
        workers = self.seed_workers(empresa, count=workers_count)
        joincodes = []
        if create_joincodes and self.JoinCode:
            joincodes = self.create_join_codes(empresa, admin, count=3)
        assignments = self.assign_workers_to_zones(workers, zonas)
        return {
            'admin': admin,
            'empresa': empresa,
            'zonas': zonas,
            'cultivos': cultivos,
            'workers': workers,
            'joincodes': joincodes,
            'assignments': assignments
        }

class Command(BaseCommand):
    help = "Seed/demo helper. Actions: seed_demo, seed_workers, seed_zones, seed_roles, seed_joincodes"

    def add_arguments(self, parser):
        parser.add_argument('action', choices=['seed_demo','seed_workers','seed_zones','seed_roles','seed_joincodes'])
        parser.add_argument('--admin-email', dest='admin_email', default='admin@example.com')
        parser.add_argument('--admin-password', dest='admin_password', default='DemoPass123!')
        parser.add_argument('--company-tax', '--empresa-tax-id', dest='company_tax', default='DEMO0001')
        parser.add_argument('--company-name', dest='company_name', default='Empresa Demo')
        parser.add_argument('--workers', dest='workers', type=int, default=5)
        parser.add_argument('--force', action='store_true', help='Force recreate (not implemented granularly yet)')
        parser.add_argument('--create-joincodes', action='store_true', dest='create_joincodes', help='Create join codes during seed_demo')
        parser.add_argument('--create-zones', action='store_true', dest='create_zones', help='Create default zones during seed_demo')
        parser.add_argument('--zone-names', dest='zone_names', help='Comma-separated zone names to create (only used with --create-zones)')

    def handle(self, *args, **options):
        action = options['action']
        s = Seeder()

        if action == 'seed_roles':
            created = s.seed_roles()
            self.stdout.write(self.style.SUCCESS(f'Roles ensured. Created: {len(created)}'))
            return

        if action == 'seed_demo':
            zone_names = None
            if options.get('zone_names'):
                zone_names = [z.strip() for z in options['zone_names'].split(',') if z.strip()]
            res = s.bulk_seed_demo(
                admin_email=options['admin_email'],
                company_tax=options['company_tax'],
                company_name=options['company_name'],
                workers_count=options['workers'],
                create_joincodes=options['create_joincodes'],
                create_zones=options['create_zones'],
                zone_names=zone_names
            )
            self.stdout.write(self.style.SUCCESS(f"Demo seeded: empresa={res['empresa'].name} admin={res['admin'].email} workers={len(res['workers'])}"))
            return

        if action == 'seed_workers':
            try:
                empresa = s.Empresa.objects.get(tax_id=options['company_tax'])
            except s.Empresa.DoesNotExist:
                raise CommandError("Empresa no encontrada. Ejecuta seed_demo primero o crea empresa.")
            workers = s.seed_workers(empresa, count=options['workers'])
            self.stdout.write(self.style.SUCCESS(f"Created {len(workers)} workers for {empresa.name}"))
            return

        if action == 'seed_zones':
            try:
                empresa = s.Empresa.objects.get(tax_id=options['company_tax'])
            except s.Empresa.DoesNotExist:
                raise CommandError("Empresa no encontrada.")
            # Si el comando pasa --zone-names, úsalo; si no, no crea nada por defecto (evitamos la trampa).
            zone_names = None
            if options.get('zone_names'):
                zone_names = [z.strip() for z in options['zone_names'].split(',') if z.strip()]
            zonas, cultivos = s.seed_zones_and_crops(empresa, zone_names=zone_names)
            self.stdout.write(self.style.SUCCESS(f"Created {len(zonas)} zonas and {len(cultivos)} cultivos for {empresa.name}"))
            return

        if action == 'seed_joincodes':
            try:
                empresa = s.Empresa.objects.get(tax_id=options['company_tax'])
            except s.Empresa.DoesNotExist:
                raise CommandError("Empresa no encontrada.")
            admin = User.objects.filter(empresa=empresa, role__code='ADMIN').first()
            if not admin:
                raise CommandError("No admin found for company. Create one first.")
            jc = s.create_join_codes(empresa, admin, count=options['workers'])
            self.stdout.write(self.style.SUCCESS(f"Created {len(jc)} joincodes for {empresa.name}"))
            return

        self.stdout.write(self.style.ERROR("Unknown action"))
