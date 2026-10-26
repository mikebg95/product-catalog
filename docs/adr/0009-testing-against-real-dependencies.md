# 9. Testing against real dependencies: Testcontainers and WireMock

**Status:** Accepted

## Context
The riskiest code is the code that talks to things outside the app: SQL that only works on PostgreSQL (full-text search, JSONB, migrations), and HTTP calls to the price system, which can be slow, down or send wrong data. Tests that fake these away don't prove much.

## Decision
- **Testcontainers:** every database test runs against a real PostgreSQL 18 in Docker, the same version as in docker-compose, with the real Flyway migrations.
- **WireMock:** the price client is tested against a fake HTTP server that answers like the real price system (following `price-api.yaml`). It can fake timeouts, `503`s, broken connections and invalid prices.
- **Around them:** plain unit tests for the domain and the services, `@WebMvcTest` for the API, one end-to-end test for the whole price sync, ArchUnit for the architecture rules, and JaCoCo that fails the build below 80% line coverage.

## Why
- When a test passes, it works on the database it really runs on. The search and the migrations only work on PostgreSQL.
- Failures of the price system are hard to cause for real, but easy and repeatable with WireMock.
- The business rules are still fast to test, because the domain depends on nothing (ADR 1).

## Not chosen
- **H2** (an in-memory database): faster and no Docker needed, but it doesn't have PostgreSQL's full-text search, and passing on H2 doesn't mean it works on PostgreSQL.
- **Mocking the HTTP client with Mockito:** then you test the mock, not the real timeouts, retries and JSON reading.
- **Using the real fake price service in tests:** slower, and it can't cause timeouts or broken data when you need them.

## Consequences
- The build needs Docker, and the database tests take a few seconds longer.
- There's no test that times "under 1 second": that would be slow and unreliable. Instead the build tests the reason it's fast (the search uses the index). The actual time was measured with a million products (ADR 6).
