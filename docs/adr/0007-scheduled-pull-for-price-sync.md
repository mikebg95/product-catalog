# 7. Scheduled pull for the price sync

**Status:** Accepted

## Context
Prices live in the price system of another team, and they change regularly. The catalog has to stay in sync with it, also when one of the two is down for a while. The assignment allows pull or push.

## Decision
Every ~10 seconds the catalog asks the price system for all prices (`GET /prices`), and saves the ones that are newer than what it already has.
- **Timeouts:** 2 s to connect, 5 s to read. A call can never hang forever.
- **Retries:** up to 3 tries per round when the connection fails, the call times out, or the answer is a `5xx`. No retry on a `4xx`: trying again won't help.
- **Only newer:** a price is only saved if its `updatedAt` (set by the price system) is newer than the one I have. So old or repeated data can never overwrite a newer price.
- **Bad data is skipped:** a price for an unknown SKU, or an invalid price, is skipped. The rest is still saved.
- **Never deletes:** an empty or incomplete answer doesn't remove any prices. While the price system is down, the catalog keeps its last known prices, and its health stays UP.

## Why
- Nothing gets lost. If the price system is down, the next round catches up. If the catalog is down, it catches up once it starts again.
- The catalog decides how often it asks, so a slow or broken price system can't overload it.
- The price system doesn't even need to know the catalog exists.
- Running a round twice does no harm, so it's also fine with several copies of the catalog (ADR 8).

## Not chosen
- **Webhooks** (the price system calls us on every change): faster, but if a change is sent while the catalog is down, it's lost unless the sender tries again. It also needs an endpoint that other systems can write to.
- **Kafka** (price changes as messages): reliable and scales well, but a whole extra system to run just for prices.

## Consequences
- A price can be up to ~10 seconds behind. That's fine for a product catalog.
- `GET /prices` returns all prices every round. With many prices, the next step is a `changedSince` filter, so you only get what changed.
- If prices must arrive within a second, or there are many more of them: use messages (Kafka) for speed, and keep a full pull now and then as a safety net.
