# Architecture Decision Records

Short notes on the big decisions in this project: what I chose, why, and what I didn't choose. In order: from the structure of the service, through how data comes in, is stored and searched, to keeping prices in sync and running it in production.

| # | Decision | Not chosen |
|---|---|---|
| [1](0001-lightweight-hexagonal-architecture.md) | Lightweight hexagonal architecture | Classic layers, strict hexagonal, full DDD |
| [2](0002-contract-first-openapi.md) | Contract-first OpenAPI | Code-first |
| [3](0003-put-products-by-sku.md) | Products come in with `PUT /products/{sku}` | `POST` + generated ID |
| [4](0004-postgresql-and-flyway.md) | PostgreSQL and Flyway | MongoDB, Liquibase |
| [5](0005-core-fields-plus-jsonb-attributes.md) | Core product fields plus flexible attributes (JSONB) | Fixed columns, key/value table |
| [6](0006-postgresql-full-text-search.md) | PostgreSQL full-text search | Elasticsearch |
| [7](0007-scheduled-pull-for-price-sync.md) | Scheduled pull for the price sync | Webhooks, Kafka |
| [8](0008-shedlock-for-the-price-sync.md) | ShedLock: one price sync, also with several copies | One copy, Quartz, leader election |
| [9](0009-testing-against-real-dependencies.md) | Testing against real dependencies: Testcontainers and WireMock | H2, mocked HTTP client |
| [10](0010-docker-compose-and-github-actions.md) | Dockerfile, docker-compose and GitHub Actions | Buildpacks, Kubernetes, Jenkins |
