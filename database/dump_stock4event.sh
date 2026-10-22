#!/usr/bin/env bash
# Gera backup do banco Stock4event (PostgreSQL).
# Uso:  ./dump_stock4event.sh [pasta_destino]
# Variaveis opcionais: PGHOST (localhost) PGPORT (5432) PGUSER (postgres) PGDATABASE (stock4event) PGPASSWORD
set -euo pipefail
DEST="${1:-./backups}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}"
DB="${PGDATABASE:-stock4event}"
STAMP="$(date +%Y%m%d_%H%M%S)"
mkdir -p "$DEST"
: "${PGPASSWORD:?Defina PGPASSWORD (export PGPASSWORD=sua_senha)}"; export PGPASSWORD

# 1) dump completo (estrutura + dados), SQL puro, restauravel com psql
pg_dump --no-owner --no-privileges --encoding=UTF8 "$DB" > "$DEST/${DB}_full_${STAMP}.sql"
# 2) so a estrutura (DDL)
pg_dump --schema-only --no-owner --no-privileges --encoding=UTF8 "$DB" > "$DEST/${DB}_schema_${STAMP}.sql"
# 3) formato custom (compacto, restaura com pg_restore)
pg_dump -Fc --no-owner --no-privileges "$DB" > "$DEST/${DB}_${STAMP}.dump"

echo "Backups gerados em $DEST:"; ls -lh "$DEST"/*"${STAMP}"*
