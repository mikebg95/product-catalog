# 8. ShedLock: one price sync, also with several copies

**Status:** Accepted

## Context
In production the catalog runs as at least two copies: if one crashes the other keeps going, and a new version can be rolled out one copy at a time without downtime. docker-compose runs two copies too.

Every copy has the scheduled price sync. Without anything extra, every copy fetches all prices every ~10 seconds. That doesn't give wrong prices, because only newer prices are saved (ADR 7), but it's double work for the price system and the database, and more with every extra copy.

## Decision
I use ShedLock, with the lock in a table in the catalog's own PostgreSQL. Before each round, a copy tries to take the lock. Only one can get it; the others skip that round.
- `lock-at-most-for: 1m`: if the copy that has the lock crashes, the lock frees itself after 1 minute. That's longer than the slowest possible call to the price system (~22 s with all retries and timeouts). With many prices, saving them takes longer too. If a round ever takes more than 1 minute, a second copy can start one as well. That can briefly save an older price; the next round fixes it.
- `lock-at-least-for: 9s`: the lock is kept for at least 9 s, even when a round only takes milliseconds. Otherwise another copy could run the same round again right after. It's just below the 10 s interval.
- The lock uses the database's clock, so copies whose clocks are a bit different still agree.

## Why
- Small: one dependency, one table, and one annotation on the scheduler. The sync code itself doesn't change.
- It uses the database that's already there.
- If a copy dies, another one takes over by itself.

## Not chosen
- **Just run one copy:** if it crashes there's nothing left, and every deploy stops the service.
- **No lock:** works, because a round can safely run twice, but every copy does all the work every round.
- **Quartz in cluster mode:** a big job scheduler with about ten tables, for one job.
- **Choosing a leader through Kubernetes:** ties the app to Kubernetes, and has more moving parts than one table.
- **A lock in Redis:** another system to run, only for this.

## Consequences
- If the copy with the lock crashes in the middle of a round, prices can be up to ~1 minute behind until another copy takes over.
- `lock-at-least-for` depends on the interval: if you change one, check the other.
