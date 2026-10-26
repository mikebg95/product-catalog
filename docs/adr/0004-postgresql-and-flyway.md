# 4. PostgreSQL and Flyway

**Status:** Accepted

## Context
Products and prices must be stored safely, the data must stay correct, and the database tables must change together with the code.

## Decision
I use PostgreSQL as the database. Flyway runs numbered SQL scripts (migrations) when the app starts. Hibernate only checks that the tables match the code (`ddl-auto: validate`), it never changes them.

## Why
- PostgreSQL is reliable, free and well known. Transactions and constraints keep the data correct: one product per SKU, and a price is either complete or not there at all (a `CHECK` on the amount and the timestamp).
- Full-text search (ADR 6) and JSONB (ADR 5) are built in, so I don't need extra systems.
- The migrations are plain SQL: what you read in the review is exactly what runs, locally, in the tests and in production.
- If the code and the tables don't match, the app doesn't start. Better than failing later on a real request.

## Not chosen
- **MongoDB:** good with flexible product data, but JSONB covers that too, and with PostgreSQL I get flexible data, constraints and full-text search in one system.
- **Liquibase:** adds changelogs in XML or YAML, rollbacks and checks before a change runs. I don't need those, and plain SQL is easier to read.
- **Letting Hibernate create the tables:** no history of changes, and no control over indexes.

## Consequences
- The price is stored in two columns in the `products` table (`price_amount`, `price_updated_at`), not in a separate table: a product has at most one current price.
- Migrations only go forward. A mistake is fixed with a new migration.
