# 1. Lightweight hexagonal architecture

**Status:** Accepted

## Context
The most important part of this service is a few business rules: only the price sync sets prices, and a price is only saved if it's newer than the one we already have. Around those rules are things that can change, like the database and the price system. I want to test the rules easily, and I don't want them to break when the technology around them changes.

## Decision
The domain (`Product`, `Price`) is in the middle and is plain Java: no Spring, no database, no HTTP. The application services (`ProductService`, `PriceSyncService`) do the actual work. Everything outside connects through adapters:
- **Going out** (my code calls something else): through an interface, a "port". `ProductRepository` is implemented by the database adapter, `PriceSource` by the price system client.
- **Coming in** (something calls my code): the REST controller and the price sync scheduler just call the services directly.

"Lightweight" means: one Maven module with packages, and only interfaces where they are really needed. No interfaces for incoming calls: an interface with one caller and one implementation adds nothing.

Inside the domain I use a few ideas from DDD (Domain-Driven Design): the domain objects hold the rules themselves (`Product.applyPrice()`, `Price.isNewerThan()`), not the services. `Price` is a value object: just an amount and a time, with no ID of its own. And `ProductRepository` works like a collection of products.

## Why
- I can unit test the business rules in milliseconds, without Spring or a database.
- If the price system starts sending prices to us instead, I only need a new adapter. The domain stays the same.
- ArchUnit tests check these rules on every build: the domain depends on nothing, and the generated API classes stay in the web adapter.

## Not chosen
- **Classic layers** (controller → service → repository, with the database entity as the model): less code, but the business rules get mixed up with the database, which makes them harder to test.
- **Strict hexagonal** (separate modules, a port for every use case): too much extra structure for a service this small.
- **Full DDD** (aggregates, domain events, long sessions with business experts to agree on the words): made for big, complicated business areas. My domain is three small classes with a few rules, so I only took the parts that help.

## Consequences
- A product exists in three forms: the API model, the domain object and the database entity. So I need mapping code between them, but each one can change on its own.
- If the service grows a lot and starts doing several different things, separate modules become worth it.
