#!/usr/bin/env bash
# Adds a million generated products to the catalog's database (or another number: ./scripts/seed-million.sh 200000).
# Demo/test tooling: writes straight into PostgreSQL, not through the API (a million PUTs would take hours).
# Run from the repo root while docker compose is up. Takes a few minutes; existing products are kept.
set -euo pipefail
cd "$(dirname "$0")/.."

N="${1:-1000000}"
echo "Adding $N products to the catalog's database..."
docker compose exec -T postgres psql -U "${POSTGRES_USER:-catalog}" -d "${POSTGRES_DB:-catalog}" -v n="$N" < scripts/seed-products.sql
echo "Done. The first search afterwards is slower (PostgreSQL loads the index into memory); do one warm-up search."
