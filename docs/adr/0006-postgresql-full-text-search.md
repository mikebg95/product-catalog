# 6. PostgreSQL full-text search

**Status:** Accepted

## Context
Users search through all product characteristics with one search field, and the results must come back in under a second.

## Decision
I use the full-text search that is built into PostgreSQL.
- An extra column, `search_vector`, is filled automatically by the database with the words of every field. Some fields count more than others: SKU, name and brand count most, then category, then description and attributes (both values and names).
- A GIN index on that column. It works like the index at the back of a book: for every word, it knows which products have it.
- Every typed word is matched as the start of a word, and all typed words must match: `red sho` finds "red shoe".
- Best match first (`ts_rank`), then sorted by SKU. If you type a full SKU, that product is found exactly and comes first.

## Why
- No extra system to run, monitor and keep in sync. The data and the search are in the same database, in the same transaction.
- A product can be found as soon as it's saved.
- It's much faster than needed, see the measurement above.

## Measured
With 1,000,008 products, every search takes under ~240 ms inside the service. The slowest case is two letters (`re`, 269,232 matches, which all have to be ranked and counted). A test runs on every build (`SearchUsesIndexTest`) and checks that the search really uses the GIN index, because that's what makes it fast.

## Not chosen
- **Elasticsearch / OpenSearch:** better at typos, synonyms and ranking on a very large scale. But it's a second system with its own copy of the data, which has to be kept in sync and can run behind.
- **`LIKE '%word%'`:** no ranking, no matching of word forms (shoe/shoes), and it can't use a normal index.

## Consequences
- No typo tolerance: `shoo` finds nothing.
- A half-typed word is sometimes shortened by PostgreSQL before matching: `recycled poly` finds nothing, while `recycled pol` does.
- Attribute names are searched too: `waterproof jacket` also finds jackets where `waterproof` is `false`.
- If typos or synonyms become important, or the catalog gets too big for one database, the next step is a search engine that gets its data from the catalog's changes.
