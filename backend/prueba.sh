#!/usr/bin/env bash
# prueba.sh — E2E backend CropCare (con imagen real o fallback)

set -Eeuo pipefail

# ====== Parámetros/flags ======
BACKEND_URL="${BACKEND_URL:-http://127.0.0.1:8000}"
DB_NAME="${DB_NAME:-cropcare}"
DB_USER="${DB_USER:-cropcare}"

ADMIN_EMAIL="${ADMIN_EMAIL:-admin@agro.cl}"
ADMIN_PASS="${ADMIN_PASS:-SuperSegura123}"
ADMIN_FIRST="${ADMIN_FIRST:-Ana}"
ADMIN_LAST="${ADMIN_LAST:-Admin}"

WORKER_EMAIL="${WORKER_EMAIL:-worker@agro.cl}"
WORKER_PASS="${WORKER_PASS:-Segura123}"

RESET_DB=false
FLUSH_DB=false
IMAGE_OVERRIDE=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --reset-db) RESET_DB=true; shift;;
    --flush) FLUSH_DB=true; shift;;
    --image) IMAGE_OVERRIDE="$2"; shift 2;;
    --url) BACKEND_URL="$2"; shift 2;;
    -h|--help)
      cat <<USAGE
Uso: ./prueba.sh [opciones]

Opciones:
  --reset-db           Dropea y recrea la BD (${DB_NAME}) y migra (requiere sudo).
  --flush              Limpia datos con 'manage.py flush --noinput' (sin sudo).
  --image <ruta>       Usa una imagen específica para la inspección.
  --url <URL>          Cambia la URL del backend (default: ${BACKEND_URL}).
  -h, --help           Muestra esta ayuda.

Variables de entorno útiles:
  ADMIN_EMAIL, ADMIN_PASS, WORKER_EMAIL, WORKER_PASS, BACKEND_URL
USAGE
      exit 0;;
    *) echo "Arg desconocido: $1"; exit 1;;
  esac
done

# ====== helpers ======
BOLD="\033[1m"; DIM="\033[2m"; GREEN="\033[32m"; RED="\033[31m"; YELLOW="\033[33m"; NC="\033[0m"
say(){ echo -e "${BOLD}$*${NC}"; }
ok(){ echo -e "${GREEN}✔ $*${NC}"; }
warn(){ echo -e "${YELLOW}⚠ $*${NC}"; }
err(){ echo -e "${RED}✘ $*${NC}" >&2; }
require_cmd(){ command -v "$1" >/dev/null 2>&1 || { err "Falta '$1'. Instálalo."; exit 1; }; }
trap 'err "Error en la línea $LINENO"; exit 1' ERR

# ====== checks ======
require_cmd curl
require_cmd jq
require_cmd python
[[ -f "manage.py" ]] || { err "No encuentro manage.py. Corre el script en la carpeta backend."; exit 1; }

# ====== reset / flush (opcional) ======
if $RESET_DB; then
  say "Reseteando BD '${DB_NAME}' (se perderán datos)…"
  sudo -u postgres psql -c "DROP DATABASE IF EXISTS ${DB_NAME};"
  sudo -u postgres psql -c "CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};"
  python manage.py migrate
  ok "BD recreada y migrada."
elif $FLUSH_DB; then
  say "Limpiando datos con 'manage.py flush'…"
  python manage.py flush --noinput
  ok "Datos limpiados."
fi

# ====== server check ======
say "Comprobando backend en ${BACKEND_URL}…"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/")
[[ "$HTTP_CODE" == "200" || "$HTTP_CODE" == "404" ]] || { err "No responde ${BACKEND_URL}. Corre: python manage.py runserver"; exit 1; }
ok "Servidor OK (HTTP ${HTTP_CODE})"

# ====== login o registro de admin ======
say "Login ADMIN (username=${ADMIN_EMAIL})…"
ADMIN_JSON="$(curl -s -X POST "${BACKEND_URL}/api/orgs/auth/token/" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASS}\"}")" || true

ACCESS="$(echo "${ADMIN_JSON}" | jq -r '.access // empty')"
REFRESH="$(echo "${ADMIN_JSON}" | jq -r '.refresh // empty')"
if [[ -z "${ACCESS}" || -z "${REFRESH}" ]]; then
  warn "No se pudo loguear. Registrando ADMIN + empresa…"
  REG_JSON="$(curl -s -X POST "${BACKEND_URL}/api/orgs/auth/register-admin/" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASS}\",\"first_name\":\"${ADMIN_FIRST}\",\"last_name\":\"${ADMIN_LAST}\",\"company_name\":\"Agro Soluciones\",\"tax_id\":\"77.777.777-7\"}")" || true
  echo "${REG_JSON}" | jq .
  ACCESS="$(echo "${REG_JSON}" | jq -r '.tokens.access // empty')"
  REFRESH="$(echo "${REG_JSON}" | jq -r '.tokens.refresh // empty')"
  [[ -n "${ACCESS}" ]] || { err "No obtuve tokens ni registrando. Revisa credenciales/logs."; exit 1; }
  ok "Admin registrado y autenticado."
else
  ok "Login admin OK."
fi
AUTH_HEADER=("Authorization: Bearer ${ACCESS}")

# ====== cultivo ======
say "Creando/Reutilizando cultivo 'Zanahoria'…"
CULTIVO_ID="$(
  python manage.py shell -c "from api.models import Cultivo; obj,_=Cultivo.objects.get_or_create(nombre='Zanahoria'); print(obj.id)" \
  | tail -n 1 | tr -dc '0-9'
)"
[[ -n "${CULTIVO_ID}" ]] || { err "No pude obtener CULTIVO_ID"; exit 1; }
ok "Cultivo ID=${CULTIVO_ID}"

# ====== join code ======
say "Creando JoinCode…"
JOIN_JSON="$(curl -s -X POST "${BACKEND_URL}/api/orgs/join-codes/" \
  -H "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d '{"max_uses": 3}')" || true
echo "${JOIN_JSON}" | jq .
JOIN_CODE="$(echo "${JOIN_JSON}" | jq -r '.code // empty')"
[[ -n "${JOIN_CODE}" ]] || { err "No obtuve JOIN_CODE"; exit 1; }
ok "JoinCode=${JOIN_CODE}"

# ====== worker ======
say "Registrando WORKER con JoinCode…"
WORKER_JSON="$(curl -s -X POST "${BACKEND_URL}/api/orgs/auth/register-worker/" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${WORKER_EMAIL}\",\"password\":\"${WORKER_PASS}\",\"join_code\":\"${JOIN_CODE}\"}")" || true
echo "${WORKER_JSON}" | jq .
WORKER_ACCESS="$(echo "${WORKER_JSON}" | jq -r '.tokens.access // empty')"
[[ -n "${WORKER_ACCESS}" ]] || { err "No pude registrar worker (code vacío/expirado/ya usado)."; exit 1; }
ok "Worker creado."

# ====== zona ======
say "Creando Zona (ADMIN)…"
ZONA_JSON="$(curl -s -X POST "${BACKEND_URL}/api/zonas/" \
  -H "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"Zona Norte\",\"cultivo\":${CULTIVO_ID}}")" || true
echo "${ZONA_JSON}" | jq .
ZONA_ID="$(echo "${ZONA_JSON}" | jq -r '.id // empty')"
[[ -n "${ZONA_ID}" && "${ZONA_ID}" != "null" ]] || { err "No obtuve ZONA_ID"; exit 1; }
ok "Zona ID=${ZONA_ID}"

# ====== plot ======
say "Creando Plot (ADMIN)…"
PLOT_JSON="$(curl -s -X POST "${BACKEND_URL}/api/plots/" \
  -H "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Lote 1\",\"zona\":${ZONA_ID},\"cultivo\":${CULTIVO_ID},\"notes\":\"ok\"}")" || true
echo "${PLOT_JSON}" | jq .
PLOT_ID="$(echo "${PLOT_JSON}" | jq -r '.id // empty')"
[[ -n "${PLOT_ID}" && "${PLOT_ID}" != "null" ]] || { err "No obtuve PLOT_ID"; exit 1; }
ok "Plot ID=${PLOT_ID}"

# ====== imagen ======
say "Buscando imagen real…"
CANDIDATES=()
if [[ -n "${IMAGE_OVERRIDE}" ]]; then
  CANDIDATES+=("${IMAGE_OVERRIDE}")
fi
CANDIDATES+=(
  "$HOME/Documentos/Zanahoria.jpg"
  "$HOME/Documentos/zanahoria.jpg"
  "$HOME/Documentos/Zanahoria.jpeg"
  "$HOME/Documentos/zanahoria.jpeg"
  "$HOME/Documentos/Zanahoria.png"
  "$HOME/Documentos/zanahoria.png"
)
IMG=""
for p in "${CANDIDATES[@]}"; do
  [[ -f "$p" ]] && IMG="$p" && break
done

if [[ -n "${IMG}" ]]; then
  ok "Usando imagen real: ${IMG}"
else
  say "No se encontró imagen real. Generando JPEG válido con Pillow…"
  require_cmd python
  # intenta importar pillow; si falta, intenta instalar
  if ! python - <<'PY' >/dev/null 2>&1
import PIL
PY
  then
    python -m pip install --quiet Pillow || { err "Instala Pillow en tu venv: pip install Pillow"; exit 1; }
  fi
  IMG="/tmp/cropcare_test.jpg"
  python - <<PY "${IMG}"
from PIL import Image
import sys
Image.new('RGB', (128, 128), (255, 128, 0)).save(sys.argv[1], 'JPEG', quality=85)
print("OK:", sys.argv[1])
PY
  ok "Imagen generada: ${IMG}"
fi

# ====== inspección ======
say "Creando Inspección con imagen…"
MIME="image/jpeg"
[[ "${IMG}" == *.png ]] && MIME="image/png"
INSP_JSON="$(curl -s -X POST "${BACKEND_URL}/api/inspections/" \
  -H "${AUTH_HEADER[@]}" \
  -F "plot=${PLOT_ID}" \
  -F "image=@${IMG};type=${MIME}" \
  -F "notes=observación breve")" || true
echo "${INSP_JSON}" | jq .
INSPECTION_ID="$(echo "${INSP_JSON}" | jq -r '.id // empty')"
[[ -n "${INSPECTION_ID}" && "${INSPECTION_ID}" != "null" ]] || { err "No obtuve INSPECTION_ID"; exit 1; }
ok "Inspección ID=${INSPECTION_ID}"

say "${GREEN}E2E COMPLETO ✔${NC}"
echo
echo "Resumen:"
echo "- Admin: ${ADMIN_EMAIL}"
echo "- Worker: ${WORKER_EMAIL}"
echo "- Cultivo ID: ${CULTIVO_ID}"
echo "- Zona ID: ${ZONA_ID}"
echo "- Plot ID: ${PLOT_ID}"
echo "- Inspección ID: ${INSPECTION_ID}"
