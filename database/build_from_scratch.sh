#!/usr/bin/env bash
# Recria o banco Stock4event do zero a partir dos scripts 01, 02 e 03 (sem precisar do backend Java).
# Uso:  ./build_from_scratch.sh [nome_do_banco]
set -euo pipefail
cd "$(dirname "$0")"; DB="${1:-stock4event}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}" PGCLIENTENCODING=UTF8
: "${PGPASSWORD:?Defina PGPASSWORD (export PGPASSWORD=sua_senha)}"; export PGPASSWORD
psql -d postgres -qc "DROP DATABASE IF EXISTS \"$DB\" WITH (FORCE);" -c "CREATE DATABASE \"$DB\" ENCODING 'UTF8';"
for f in 01_schema.sql 02_seed_catalogo.sql 03_seed_demo.sql; do echo ">> $f"; psql -d "$DB" -v ON_ERROR_STOP=1 -q -f "$f" 2>&1 | grep -v NOTICE || true; done
echo "Banco '$DB' pronto. Login demo: demo@stock4event.com / demo1234"
