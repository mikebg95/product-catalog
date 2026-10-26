# 5. Core product fields plus flexible attributes (JSONB)

**Status:** Accepted

## Context
Different products have different characteristics: a shoe has a size, a laptop has RAM. And the search has to look through all of them ("all product characteristics").

## Decision
Every product has the same basic fields: `name`, `description`, `brand` and `category`. Everything else goes into a simple `attributes` list of names and values, like `{"color": "red", "ramGb": 16}`. In PostgreSQL the basic fields are normal columns, and `attributes` is one JSONB column.

## Why
- With only fixed columns, the table would need a column for every possible characteristic, and most of them would be empty.
- The fields every product has stay easy to check, index and read.
- PostgreSQL can search inside JSONB, so the attributes are part of the full-text search (ADR 6).

## Not chosen
- **Only fixed columns:** every new kind of characteristic needs a new migration.
- **A separate table with one row per attribute:** just as flexible, but every read needs a join, and searching gets harder.
- **A document database:** see ADR 4.

## Consequences
- The database doesn't check the types of the values in `attributes`. The API file only allows simple values: text, numbers and true/false.
- If we ever need to filter or sort on attributes (like "RAM above 16 GB"), the important ones should become their own columns or get their own index.
