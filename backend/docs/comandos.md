# Demo seed - CropCare Backend

Requisitos:
- Python v3.12 (virtualenv activado)
- Dependencias: pip install -r requirements.txt
- Config .env: DATABASE_URL, AWS_*, SECRET_KEY

Pasos (local - sqlite):

1. Instala dependencias
   $ python -m venv .venv
   $ source .venv/bin/activate
   $ pip install -r requirements.txt

2. Migraciones
   $ python manage.py makemigrations
   $ python manage.py migrate

3. Ejecuta seed demo
   $ python manage.py seed_demo seed_demo --admin-email=admin@demo.test --company-tax=DEMO001 --company-name="Empresa Demo" --workers=5 --create-joincodes

   Salida: confirma empresa/admin/ workers / join codes creados.

4. Login (JWT)
   - POST /api/accounts/auth/login/  (email/password) para obtener token.
   - Usa token para llamadas a endpoints protegidos.

5. Probar join code:
   - Usa uno de los join codes creados con seed_demo.
   - Endpoint: POST /api/joincodes/join-company/ { "join_code": "ABC123" }

6. Probar scanner (simulación):
   - Crear una ScannerSession via POST /api/scanners/sessions/
   - Subir imagen (multipart) a /api/scanners/images/
   - Subir o enviar results a /api/scanners/results/
   - Revisar /api/reports/session-reports/ para reportes generados (si Celery configurado; si no, run sync report tasks manually).

Ejemplos de código :

Crear admin + empresa

python manage.py seed_demo seed_demo --admin-email=admin@demo.com --admin-password=Demo123! --company-tax=DEMO123 --company-name="Demo Co"

crear trabajadores con tablas que existen:

- python manage.py seed_demo seed_workers --company-tax=DEMO123 --workers=10

Crea X workers con password por defecto WorkerPass123! (  flag --worker-password)
crear zona y cultivo:
python manage.py seed_demo seed_zones --company-tax=DEMO123

crear join_code:

- python manage.py seed_demo seed_joincodes --company-tax=DEMO123 --workers=3

Crea X join codes (usa created_by = el admin encontrado en esa empresa).

Notas:
- El management command es **para uso interno/CI/demo**. No expongas su funcionalidad a clientes.
- Para producción: configura S3 en settings con `django-storages` y `DEFAULT_FILE_STORAGE`.



Especificación y facilitación de creación de tablas y usuarios:

creación de admin facilitada:

new/Backend/cropcareBackend on  master [!+?] via 🐍 v3.12.3 (venv) 
❯ python manage.py seed_demo seed_demo \
  --admin-email=admin@demo.com \
  --admin-password='Demo123!' \
  --company-tax=DEMO123 \
  --company-name="Demo Co" \
  --workers=0


# creación de zonas facilitada:

new/Backend/cropcareBackend on  master [!+?] via 🐍 v3.12.3 (venv) 
❯ python manage.py shell <<'PY'
from django.apps import apps
Empresa = apps.get_model('emprises','Empresa')
Zona = apps.get_model('zonecrop','Zona')
Cultivo = apps.get_model('zonecrop','Cultivo')

e = Empresa.objects.get(tax_id='DEMO123')

# Zona A
zona_a, created = Zona.objects.get_or_create(nombre='Zona A', empresa=e, defaults={'descripcion':'Zona A demo'})
Cultivo.objects.get_or_create(nombre='Papas Zona A', zona=zona_a)
Cultivo.objects.get_or_create(nombre='Zanahoria Zona A', zona=zona_a)

# Zona B
zona_b, created = Zona.objects.get_or_create(nombre='Zona B', empresa=e, defaults={'descripcion':'Zona B demo'})
Cultivo.objects.get_or_create(nombre='Papas Zona B', zona=zona_b)
Cultivo.objects.get_or_create(nombre='Tomate Zona B', zona=zona_b)

print("Zonas y cultivos asegurados:", zona_a.nombre, zona_b.nombre)
PY

generación de código:

❯ python manage.py shell <<'PY'
from django.apps import apps
from django.utils import timezone
JoinCode = apps.get_model('joincodes','JoinCode')
Empresa = apps.get_model('emprises','Empresa')
e = Empresa.objects.get(tax_id='DEMO123')

jc = JoinCode.objects.create(empresa=e, created_by=e.owner, max_uses=12,
                             expires_at=timezone.now() + timezone.timedelta(days=30))
print("JOINCODE_RANDOM:", jc.code)
PY

# creación de usuarios por join code (auth proceso)

bash -c '
JOINCODE="ATQ3TNSJ"
for i in 1 2 3 4; do
  email="worker_nozone${i}@democo.demo"
  jq -n --arg e "$email" --arg pw "WorkerDemo123!" --arg fn "NoZone${i}" --arg ln "Demo" --arg ph "+5691000${i}" --arg jc "$JOINCODE" \
    "{email:\$e, password:\$pw, first_name:\$fn, last_name:\$ln, phone:\$ph, join_code:\$jc}" \
  | curl -s -X POST http://127.0.0.1:8000/api/accounts/auth/register-worker/ -H "Content-Type: application/json" --data-binary @- | jq .
done
'


# asignar usuarios y crear usuarios

python manage.py seed_workers_faker --company-tax=DEMO123 \
  --no-zone=4 --zone-a=4 --zone-b=4 --password='WorkerDemo123!'


pero en este caso lo agrega por secciones en sin zonas, luego a zona a y zona 4 son 4 workers respectivamente


