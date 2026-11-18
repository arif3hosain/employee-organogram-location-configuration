#!/usr/bin/env bash
# Wipes the POC database and lets Flyway rebuild it (V1..V4) on the next start.
# Usage:  bash scripts/reset-db.sh            then: mvn spring-boot:run
# Override with DB_HOST / DB_PORT / DB_NAME / DB_USER / PGPASSWORD if needed.
set -euo pipefail

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="${DB_NAME:-pmspoc}"
DB_USER="${DB_USER:-postgres}"
export PGPASSWORD="${PGPASSWORD:-postgres}"

psql_admin() { psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -v ON_ERROR_STOP=1 "$@"; }

# create the database if it was dropped
if ! psql_admin -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1; then
  echo "Creating database $DB_NAME"
  psql_admin -d postgres -c "CREATE DATABASE $DB_NAME"
fi

# drop every table, including flyway_schema_history, so Flyway runs V1..V4 again
echo "Resetting schema 'public' in $DB_NAME"
psql_admin -d "$DB_NAME" -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;"

echo "Done. Now start the backend (Flyway runs all migrations):  mvn spring-boot:run"
