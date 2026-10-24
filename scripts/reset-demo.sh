#!/usr/bin/env bash
# Empties the demo: all products in the catalog's database, and all SKUs and prices in fake-price-service.
# An ops action from the terminal on purpose: the catalog has no "delete everything" endpoint, and shouldn't.
# Run from the repo root while docker compose is up. Afterwards, click "Clear this list" in fake-pim
# (its products live in the browser, a script can't reach them).
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Catalog: removing all products..."
docker compose exec -T postgres psql -U "${POSTGRES_USER:-catalog}" -d "${POSTGRES_DB:-catalog}" -q -c "TRUNCATE products;"

echo "Price system: removing all SKUs and prices..."
curl -fsS -X POST http://localhost:8081/demo/outage/reset > /dev/null

echo "Done. Now click \"Clear this list\" in fake-pim (http://localhost:3001)."
