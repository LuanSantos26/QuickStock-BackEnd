#!/usr/bin/env bash
# Restaura um dump SQL em um banco NOVO (recria o banco se ja existir, com confirmacao).
# Uso:  ./restore_stock4event.sh arquivo.sql [nome_do_banco]
set -euo pipefail
FILE="${1:?Informe o arquivo .sql do dump}"; DB="${2:-stock4event}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}"
: "${PGPASSWORD:?Defina PGPASSWORD (export PGPASSWORD=sua_senha)}"; export PGPASSWORD
if psql -d postgres -Atc "select 1 from pg_database where datname='$DB'" | grep -q 1; then
  read -r -p "O banco '$DB' ja existe e sera APAGADO. Digite o nome para confirmar: " ok
  [ "$ok" = "$DB" ] || { echo "Cancelado."; exit 1; }
  psql -d postgres -qc "DROP DATABASE \"$DB\" WITH (FORCE);"
fi
psql -d postgres -qc "CREATE DATABASE \"$DB\" ENCODING 'UTF8';"
psql -d "$DB" -v ON_ERROR_STOP=1 -q -f "$FILE"
echo "Restauracao concluida no banco '$DB'."
