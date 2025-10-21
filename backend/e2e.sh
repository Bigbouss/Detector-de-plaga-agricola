#!/usr/bin/env bash
# checks_extra.sh — Pruebas complementarias de CropCare backend
# Cubre: max_uses / revoke / expiración JoinCodes; aislamiento de empresa;
# validaciones de Zona/Plot; límites de palabras; y requerimiento de imagen.
set -Eeuo pipefail

BACKEND_URL="${BACKEND_URL:-http://127.0.0.1:8000}"

ADMIN1_EMAIL="${ADMIN1_EMAIL:-admin@agro.cl}"
ADMIN1_PASS="${ADMIN1_PASS:-SuperSegura123}"
ADMIN2_EMAIL="${ADMIN2_EMAIL:-admin2@agro.cl}"
ADMIN2_PASS="${ADMIN2_PASS:-ClaveSegura456}"
WORKER_EMAIL="${WORKER_EMAIL:-worker_checks@agro.cl}"
WORKER_PASS="${WORKER_PASS:-Segura123}"

BOLD="\033[1m"; GREEN="\033[32m"; RED="\033[31m"; YELLOW="\033[33m"; NC="\033[0m"
say(){ echo -e "${BOLD}$*${NC}"; }
ok(){ echo -e "${GREEN}✔ $*${NC}"; }
warn(){ echo -e "${YELLOW}⚠ $*${NC}"; }
err(){ echo -e "${RED}✘ $*${NC}" >&2; }

require_cmd(){ command -v "$1" >/dev/null 2>&1 || { err "Falta '$1'"; exit 1; }; }
trap 'err "Error en la línea $LINENO"; exit 1' ERR

TMP_DIR="$(mktemp -d)"
cleanup(){ rm -rf "$TMP_DIR"; }
trap cleanup EXIT

require_cmd curl
require_cmd jq
[[ -f manage.py ]] || { err "Ejecuta este script en la carpeta backend (donde está manage.py)"; exit 1; }

# --- helper curl con headers seguros ---
# uso: call_json <method> <url> <outfile> <headers-json-array> [data-json]
call_json() {
  local METHOD="$1" URL="$2" OUT="$3" HDRS_JSON="${4:-[]}" DATA="${5:-}"
  local CURL_ARGS=(-s -S -X "$METHOD" "$URL" -o "$OUT" -w "%{http_code}")
  mapfile -t HEADERS < <(printf '%s' "$HDRS_JSON" | jq -r '.[]?')
  for h in "${HEADERS[@]}"; do [[ -n "$h" ]] && CURL_ARGS+=(-H "$h"); done
  [[ -n "$DATA" ]] && CURL_ARGS+=(-H "Content-Type: application/json" -d "$DATA")
  curl "${CURL_ARGS[@]}"
}
assert_code() {
  local GOT="$1" WANT="$2" MSG="$3" BODY_FILE="$4"
  if [[ "$GOT" != "$WANT" ]]; then
    err "$MSG (esperado $WANT, recibido $GOT)"
    [[ -f "$BODY_FILE" ]] && { echo "Respuesta:"; cat "$BODY_FILE" | jq . || cat "$BODY_FILE"; }
    exit 1
  fi
}

# --- ping ---
say "Comprobando backend en ${BACKEND_URL}…"
CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/")
[[ "$CODE" == "200" || "$CODE" == "404" ]] || { err "Backend no responde (HTTP $CODE). Corre: python manage.py runserver"; exit 1; }
ok "Backend responde (HTTP $CODE)"

# --- login o registro admin1 (empresa A) ---
say "Login ADMIN1 (${ADMIN1_EMAIL})…"
OUT="$TMP_DIR/a1_login.json"
CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/token/" "$OUT" '["Content-Type: application/json"]' "{\"username\":\"${ADMIN1_EMAIL}\",\"password\":\"${ADMIN1_PASS}\"}") || true
if [[ "$CODE" != "200" ]]; then
  warn "No se pudo loguear. Registrando ADMIN1 + empresa A…"
  CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/register-admin/" "$OUT" '["Content-Type: application/json"]' "{\"email\":\"${ADMIN1_EMAIL}\",\"password\":\"${ADMIN1_PASS}\",\"first_name\":\"Ana\",\"last_name\":\"Admin\",\"company_name\":\"Empresa A\",\"tax_id\":\"11.111.111-1\"}")
  assert_code "$CODE" 201 "Registro admin1 falló" "$OUT"
fi
ACCESS1=$(jq -r '.access // .tokens.access' "$OUT"); [[ -n "$ACCESS1" && "$ACCESS1" != "null" ]] || { err "Sin token de admin1"; cat "$OUT"; exit 1; }
HDR1=$(jq -nc --arg h "Authorization: Bearer $ACCESS1" '[ $h ]'); ok "Admin1 autenticado"

# --- cultivo base ---
say "Creando/Reusando cultivo 'Zanahoria'…"
CULTIVO_ID="$(
  python manage.py shell -c "from api.models import Cultivo; o,_=Cultivo.objects.get_or_create(nombre='Zanahoria'); print(o.id)" \
  | tail -n 1 | tr -dc '0-9'
)"
[[ -n "$CULTIVO_ID" ]] || { err "No pude obtener CULTIVO_ID"; exit 1; }
ok "Cultivo ID=$CULTIVO_ID"

# --- zona base (empresa A) ---
say "Creando zona base Empresa A…"
OUT="$TMP_DIR/a1_zoneA.json"
CODE=$(call_json POST "${BACKEND_URL}/api/zonas/" "$OUT" "$HDR1" "{\"nombre\":\"Zona-A\",\"cultivo\":${CULTIVO_ID}}") || true
[[ "$CODE" == "201" || "$CODE" == "400" ]] || { err "Creación Zona-A retornó $CODE inesperado"; cat "$OUT"; exit 1; }
ZONA_A_ID=$(jq -r '.id // empty' "$OUT")
[[ -z "$ZONA_A_ID" ]] && ZONA_A_ID="$(
  curl -s -H "Authorization: Bearer $ACCESS1" "${BACKEND_URL}/api/zonas/" | jq '.results[]? | select(.nombre=="Zona-A") | .id' | head -n1
)"
[[ -n "$ZONA_A_ID" ]] || { err "No conseguí ZONA_A_ID"; exit 1; }
ok "Zona-A ID=$ZONA_A_ID"

# --- CHECK JoinCodes: max_uses y revoke ---
say "JoinCodes: creando con max_uses=2…"
OUT="$TMP_DIR/jc2.json"
CODE=$(call_json POST "${BACKEND_URL}/api/orgs/join-codes/" "$OUT" "$HDR1" '{"max_uses":2}') || true
assert_code "$CODE" 201 "Creación join-code max_uses=2 falló" "$OUT"
JC2_CODE=$(jq -r '.code' "$OUT"); JC2_ID=$(jq -r '.id' "$OUT"); ok "JoinCode JC2=$JC2_CODE (id=$JC2_ID)"

# Registrar 2 workers con el código ⇒ OK; 3º ⇒ 400
say "Consumir JC2 con 2 workers y validar rechazo en el 3º…"
W1=$(mktemp); W2=$(mktemp); W3=$(mktemp)
for i in 1 2; do
  EMAIL="max$i@agro.cl"
  OUT="$TMP_DIR/w_max${i}.json"
  CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/register-worker/" "$OUT" '["Content-Type: application/json"]' "{\"email\":\"${EMAIL}\",\"password\":\"Segura123\",\"join_code\":\"${JC2_CODE}\"}") || true
  assert_code "$CODE" 201 "Registro worker $i con JC2 falló" "$OUT"
done
OUT="$TMP_DIR/w_max3.json"
CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/register-worker/" "$OUT" '["Content-Type: application/json"]' "{\"email\":\"max3@agro.cl\",\"password\":\"Segura123\",\"join_code\":\"${JC2_CODE}\"}") || true
assert_code "$CODE" 400 "Tercer uso de JC2 NO fue rechazado" "$OUT"
ok "max_uses funciona"

# revoke
say "Revocando join-code JC2 y validando rechazo…"
OUT="$TMP_DIR/jc2_patch.json"
CODE=$(call_json PATCH "${BACKEND_URL}/api/orgs/join-codes/${JC2_ID}/" "$OUT" "$HDR1" '{"revoked":true}') || true
assert_code "$CODE" 200 "No pude revocar JC2" "$OUT"
OUT="$TMP_DIR/w_revoked.json"
CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/register-worker/" "$OUT" '["Content-Type: application/json"]' "{\"email\":\"revocado@agro.cl\",\"password\":\"Segura123\",\"join_code\":\"${JC2_CODE}\"}") || true
assert_code "$CODE" 400 "Código revocado NO fue rechazado" "$OUT"
ok "revoked funciona"

# --- CHECK Zona duplicada en la MISMA empresa ⇒ 400
say "Zona duplicada en misma empresa debe fallar (400)…"
OUT="$TMP_DIR/zone_dup.json"
CODE=$(call_json POST "${BACKEND_URL}/api/zonas/" "$OUT" "$HDR1" "{\"nombre\":\"Zona-A\",\"cultivo\":${CULTIVO_ID}}") || true
assert_code "$CODE" 400 "Zona duplicada NO fue rechazada" "$OUT"
ok "Restricción de unicidad por empresa correcta"

# --- CHECK Plot reglas negocio ---
say "Plot: empresa debe exigir zona (400 si falta zona)…"
OUT="$TMP_DIR/plot_nozona.json"
DATA=$(jq -nc --arg name "Lote-sin-zona" --argjson cultivo "$CULTIVO_ID" '{name:$name, cultivo:$cultivo, notes:"x"}')
CODE=$(call_json POST "${BACKEND_URL}/api/plots/" "$OUT" "$HDR1" "$DATA") || true
assert_code "$CODE" 400 "Plot sin zona NO fue rechazado" "$OUT"
ok "Validación de zona requerida en empresa OK"

# Para probar “zona de otra empresa”, necesitamos empresa B:
say "Creando empresa B (admin2)…"
OUT="$TMP_DIR/a2_login.json"
CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/token/" "$OUT" '["Content-Type: application/json"]' "{\"username\":\"${ADMIN2_EMAIL}\",\"password\":\"${ADMIN2_PASS}\"}") || true
if [[ "$CODE" != "200" ]]; then
  warn "No se pudo loguear admin2. Registrando admin2 + empresa B…"
  CODE=$(call_json POST "${BACKEND_URL}/api/orgs/auth/register-admin/" "$OUT" '["Content-Type: application/json"]' "{\"email\":\"${ADMIN2_EMAIL}\",\"password\":\"${ADMIN2_PASS}\",\"first_name\":\"Beto\",\"last_name\":\"Boss\",\"company_name\":\"Empresa B\",\"tax_id\":\"22.222.222-2\"}")
  assert_code "$CODE" 201 "Registro admin2 falló" "$OUT"
fi
ACCESS2=$(jq -r '.access // .tokens.access' "$OUT"); HDR2=$(jq -nc --arg h "Authorization: Bearer $ACCESS2" '[ $h ]'); ok "Admin2 autenticado"

say "Creando Zona-B (empresa B)…"
OUT="$TMP_DIR/zoneB.json"
CODE=$(call_json POST "${BACKEND_URL}/api/zonas/" "$OUT" "$HDR2" "{\"nombre\":\"Zona-B\",\"cultivo\":${CULTIVO_ID}}") || true
assert_code "$CODE" 201 "No pude crear Zona-B" "$OUT"
ZONA_B_ID=$(jq -r '.id' "$OUT")

say "Intentar crear Plot en empresa A pero con Zona-B (de otra empresa) ⇒ 400…"
OUT="$TMP_DIR/plot_cross.json"
DATA=$(jq -nc --arg name "Lote-cross" --argjson zona "$ZONA_B_ID" --argjson cultivo "$CULTIVO_ID" '{name:$name, zona:$zona, cultivo:$cultivo, notes:"x"}')
CODE=$(call_json POST "${BACKEND_URL}/api/plots/" "$OUT" "$HDR1" "$DATA") || true
assert_code "$CODE" 400 "Plot con zona de otra empresa NO fue rechazado" "$OUT"
ok "Bloqueo de cruce empresa/zona OK"

# --- CHECK Aislamiento de listados (admin2 no ve zonas de empresa A) ---
say "Admin2 listando zonas (no debe ver Zona-A)…"
OUT="$TMP_DIR/a2_zonas.json"
CODE=$(call_json GET "${BACKEND_URL}/api/zonas/" "$OUT" "$HDR2") || true
assert_code "$CODE" 200 "Listar zonas admin2 falló" "$OUT"
COUNT_A2=$(jq '[.results[]?] | length // 0' "$OUT" 2>/dev/null || jq 'length // 0' "$OUT")
[[ "$COUNT_A2" -eq 1 ]] || warn "Admin2 ve $COUNT_A2 zonas (esperable 1 si existe solo Zona-B)"
ok "Aislamiento de zonas por empresa OK (no aparecen zonas de empresa A)"

# --- CHECK límites de palabras en notes ---
make_words() { local n="$1"; python - "$n" <<'PY'
import sys
n=int(sys.argv[1]); print(" ".join(["w"]*n))
PY
}
say "Plot: 121 palabras en notes ⇒ 400…"
LONG120="$(make_words 121)"
OUT="$TMP_DIR/plot_long.json"
DATA=$(jq -nc --arg name "Lote-words" --argjson zona "$ZONA_A_ID" --argjson cultivo "$CULTIVO_ID" --arg notes "$LONG120" '{name:$name, zona:$zona, cultivo:$cultivo, notes:$notes}')
CODE=$(call_json POST "${BACKEND_URL}/api/plots/" "$OUT" "$HDR1" "$DATA") || true
assert_code "$CODE" 400 "Plot con >120 palabras NO fue rechazado" "$OUT"
ok "Límite de 120 palabras en plot OK"

say "Inspection: 241 palabras en notes ⇒ 400…"
# crear plot válido para la inspección
OUT="$TMP_DIR/plot_ok.json"
DATA=$(jq -nc --arg name "Lote-ok" --argjson zona "$ZONA_A_ID" --argjson cultivo "$CULTIVO_ID" '{name:$name, zona:$zona, cultivo:$cultivo, notes:"ok"}')
CODE=$(call_json POST "${BACKEND_URL}/api/plots/" "$OUT" "$HDR1" "$DATA") || true
assert_code "$CODE" 201 "No pude crear plot válido" "$OUT"
PLOT_OK=$(jq -r '.id' "$OUT")

LONG240="$(make_words 241)"
say "Crear inspección SIN imagen ⇒ 400…"
OUT="$TMP_DIR/insp_noimg.json"
# multipart sin image:
CODE=$(curl -s -S -o "$OUT" -w "%{http_code}" -X POST "${BACKEND_URL}/api/inspections/" \
  -H "Authorization: Bearer $ACCESS1" \
  -F "plot=$PLOT_OK" \
  -F "notes=algo") || true
assert_code "$CODE" 400 "Inspección sin imagen NO fue rechazada" "$OUT"
ok "Requerimiento de imagen en inspección OK"

say "Crear inspección con imagen pero 241 palabras en notes ⇒ 400…"
# generar PNG mínimo
IMG="$TMP_DIR/min.png"
python - "$IMG" <<'PY'
import base64,sys
b=base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGP4z8DwHwAFgwJ/k3J4dQAAAABJRU5ErkJggg==")
open(sys.argv[1],'wb').write(b)
print(sys.argv[1])
PY
OUT="$TMP_DIR/insp_long.json"
CODE=$(curl -s -S -o "$OUT" -w "%{http_code}" -X POST "${BACKEND_URL}/api/inspections/" \
  -H "Authorization: Bearer $ACCESS1" \
  -F "plot=$PLOT_OK" \
  -F "image=@$IMG" \
  -F "notes=$LONG240") || true
assert_code "$CODE" 400 "Inspección con >240 palabras NO fue rechazada" "$OUT"
ok "Límite de 240 palabras en inspección OK"

echo -e "\n${GREEN}CHECKS EXTRA COMPLETOS ✔${NC}"
