#!/usr/bin/env bash
# e2e.sh — Pruebas end-to-end para CropCare API (Capstone)
# Requisitos: curl, jq
# Supone: servidor corriendo en http://127.0.0.1:8000 y superusuario admin/admin creado

set -Eeuo pipefail

BASE="${BASE:-http://127.0.0.1:8000}"
API="$BASE/api/v1/cropcare"
SCHEMA="$BASE/api/schema/"
DOCS="$BASE/api/docs/"
SUFFIX="$(date +%s)"            # evita conflictos por unique_together(owner, name) en Plot
PLOT_NAME="Lote Norte $SUFFIX"

hr(){ printf "\n%s\n" "────────────────────────────────────────────────────────"; }
step(){ printf "\n▶ %s\n" "$*"; }
ok(){ printf "✅ %s\n" "$*"; }
err(){ printf "❌ %s\n" "$*" >&2; exit 1; }

need() { command -v "$1" >/dev/null 2>&1 || err "Necesitas '$1' instalado"; }
need curl; need jq

# Helpers JSON
post_json(){ curl -sS -X POST "$1" -H "Content-Type: application/json" -d "$2"; }
auth_post_json(){ curl -sS -X POST "$1" -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" -d "$2"; }
patch_json(){ curl -sS -X PATCH "$1" -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" -d "$2"; }
auth_get(){ curl -sS "$1" -H "Authorization: Bearer $ACCESS"; }
auth_delete(){ curl -sS -i -X DELETE "$1" -H "Authorization: Bearer $ACCESS"; }

hr
step "Status del servicio"
curl -sS "$BASE/status/" | jq -e '.status=="ok"' >/dev/null && ok "Status OK" || err "Status no OK"

step "API root"
ROOT_JSON=$(curl -sS "$API/" | jq .) || err "No se pudo leer root"
echo "$ROOT_JSON" | jq .
ok "Root OK"

step "OpenAPI schema & Swagger"
curl -sS -I "$SCHEMA" >/dev/null && ok "Schema disponible (descarga YAML)"
curl -sS -I "$DOCS"   >/dev/null && ok "Swagger UI disponible"

hr
step "Login ADMIN → tokens"
TOKENS_ADMIN=$(post_json "$API/auth/jwt/create/" '{"username":"admin","password":"admin"}') || err "Login admin falló"
ACCESS_ADMIN=$(echo "$TOKENS_ADMIN" | jq -er '.access') || err "Sin access admin"
REFRESH_ADMIN=$(echo "$TOKENS_ADMIN" | jq -er '.refresh') || err "Sin refresh admin"
ok "Admin autenticado"

step "Registrar usuaria ANA (idempotente)"
CODE=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$API/auth/register/" \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","email":"ana@example.com","password":"CLAVE_ANA"}')
if [ "$CODE" = "201" ]; then ok "Ana registrada"
elif [ "$CODE" = "400" ]; then ok "Registro de Ana ya existía (idempotente)"
else err "Registro Ana devolvió HTTP $CODE"
fi

step "Login ANA → tokens"
TOKENS=$(post_json "$API/auth/jwt/create/" '{"username":"ana","password":"CLAVE_ANA"}') || err "Login ana falló"
echo "$TOKENS" | jq .
ACCESS=$(echo "$TOKENS" | jq -er '.access') || err "Sin access"
REFRESH=$(echo "$TOKENS" | jq -er '.refresh') || err "Sin refresh"
ok "Obtuve ACCESS y REFRESH"

step "Verificar ACCESS"
post_json "$API/auth/jwt/verify/" "{\"token\":\"$ACCESS\"}" | jq -e 'type=="object"' >/dev/null && ok "ACCESS válido" || err "ACCESS inválido"

step "Ping autenticado"
auth_get "$API/ping/" | jq .
ok "Ping OK"

hr
step "Perfil (GET)"
auth_get "$API/auth/profile/" | jq .
ok "Perfil leído"

step "Perfil (PATCH)"
patch_json "$API/auth/profile/" '{"display_name":"Ana T.","organization":"CropCare","phone":"+56 9 1234 5678"}' | jq .
ok "Perfil actualizado"

hr
step "Crear Plot (Lote)"
P1=$(auth_post_json "$API/plots/" "{\"name\":\"$PLOT_NAME\",\"cultivo\":\"tomate\",\"superficie_ha\":1.5,\"fecha_siembra\":\"2025-09-01\",\"notes\":\"ensayo $SUFFIX\"}" \
      | jq .) || err "No se pudo crear Plot"
echo "$P1"
PLOT_ID=$(echo "$P1" | jq -er '.id')
ok "Plot creado ($PLOT_ID)"

step "Listar/filtrar/ordenar Plots"
auth_get "$API/plots/?page=1&search=tomate&ordering=-created_at" | jq .
ok "List/filters OK"

step "Detalle Plot"
auth_get "$API/plots/$PLOT_ID/" | jq .
ok "Detalle OK"

step "Actualizar Plot (PATCH)"
patch_json "$API/plots/$PLOT_ID/" '{"notes":"ensayo actualizado"}' | jq .
ok "Plot actualizado"

step "Negativo: Crear Plot inválido (espera 400)"
BAD_CODE=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$API/plots/" -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" -d '{"name":""}')
[ "$BAD_CODE" = "400" ] && ok "Validaciones OK (400 en payload inválido)" || err "Se esperaba 400, devolvió $BAD_CODE"

hr
step "Crear Inspección"
I1=$(auth_post_json "$API/inspections/" "{\"plot\":\"$PLOT_ID\",\"notes\":\"hojas con manchas\"}" | jq .) || err "No se pudo crear inspección"
echo "$I1"
INSP_ID=$(echo "$I1" | jq -er '.id')
ok "Inspección creada ($INSP_ID)"

step "Crear Diagnóstico"
D1=$(auth_post_json "$API/diagnostics/" "{\"inspection\":\"$INSP_ID\",\"label\":\"tizon_tardio\",\"confidence\":0.92}" | jq .) || err "Diagnóstico falló"
echo "$D1"
DIAG_ID=$(echo "$D1" | jq -er '.id')
ok "Diagnóstico creado ($DIAG_ID)"

step "Eliminar Diagnóstico"
auth_delete "$API/diagnostics/$DIAG_ID/" | grep -q "204 No Content" && ok "Diagnóstico eliminado" || err "No se pudo eliminar diagnóstico"

hr
step "Crear Report (historial de reportes) 1"
R1=$(auth_post_json "$API/reports/" "{\"title\":\"Reporte $SUFFIX A\",\"plot\":\"$PLOT_ID\"}" | jq .) || err "No se pudo crear Report A"
echo "$R1"
R1_ID=$(echo "$R1" | jq -er '.id')
ok "Report A creado ($R1_ID)"

step "Crear Report (historial de reportes) 2"
R2=$(auth_post_json "$API/reports/" "{\"title\":\"Reporte $SUFFIX B\",\"plot\":\"$PLOT_ID\"}" | jq .) || err "No se pudo crear Report B"
echo "$R2"
R2_ID=$(echo "$R2" | jq -er '.id')
ok "Report B creado ($R2_ID)"

step "Marcar Report B como READY"
patch_json "$API/reports/$R2_ID/" '{"status":"ready"}' | jq .
ok "Report B → READY"

step "Listar Reports filtrando status=ready, ordering=-created_at"
auth_get "$API/reports/?status=ready&ordering=-created_at" | jq .
ok "Listado filtrado OK"

hr
step "Refrescar token (REFRESH → NEW_ACCESS)"
NEW_ACCESS=$(post_json "$API/auth/jwt/refresh/" "{\"refresh\":\"$REFRESH\"}" | jq -er '.access')
[ -n "$NEW_ACCESS" ] && ok "Refresh OK" || err "Refresh falló"

hr
step "Probar aislamiento por usuario (ADMIN vs ANA)"
ADMIN_PLOT_CODE=$(curl -sS -o /dev/null -w "%{http_code}" "$API/plots/$PLOT_ID/" -H "Authorization: Bearer $ACCESS_ADMIN")
[ "$ADMIN_PLOT_CODE" = "404" ] && ok "Ownership aplicado (admin no ve lotes ajenos por API)" || err "Se esperaba 404, devolvió $ADMIN_PLOT_CODE"

hr
ok "👌 TODO OK — E2E completo"
