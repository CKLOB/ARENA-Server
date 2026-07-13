# CLAUDE.md

Guide for working in the Spring Boot backend repo.

## Project Context

Main backend for an AI mock investment battle platform. Users compete against AI in 7-day mock trading challenges. This repo owns **domain logic where correctness matters** — trading, challenges, portfolio. The actual trading decisions and recommendation generation live in a separate FastAPI AI service; this repo only consumes those results.

- This is not a real trading service (education positioning). Do not add features that monetize or encourage trading frequency.

## Stack

- Kotlin + Spring Boot, Spring Data JPA, Spring Security, JWT
- PostgreSQL, Redis
- Gradle (Kotlin DSL)

## Domain Module Structure

`user` · `market` · `challenge` · `trading` · `portfolio` · `recommendation` (reaction logging) · `learning`

Follow [ARCHITECTURE.md](ARCHITECTURE.md) for the required `domain` and `global` package layout.

## Boundary with the FastAPI AI Service

- This repo does not implement trading decision or recommendation generation logic. It calls the FastAPI service over HTTP and only processes the result (action, recommended symbol, probability).
- Every response from FastAPI must trigger a decision log write (this is a prerequisite for the observability system).
- If the API contract changes, update the OpenAPI spec in the same change — web and iOS clients consume this repo's API directly.

## Market Handling

- Market type is an enum: `KR | US | COIN`. Currently active: `US`, `COIN`. `KR` is disabled behind a feature flag pending brokerage account setup.
- Trading hours, currency unit, and price limit rules must be market-specific config, never hardcoded.
- Price lookups: check Redis cache first, only call the external API on a cache miss. Never cache without a TTL.

## Transaction Rules

- Trade execution (`order → execution → balance update`) must be wrapped in a **single transaction**. No partial failures allowed.
- Challenge settlement (win/loss determination, final return calculation) must also be transactional.
- User reactions to AI recommendations (accept/reject/ignore) must always be persisted — this is the foundation for retraining and observability data.

## Commit / PR Conventions

- Commit message format: `feat :: description`
- No prefixes on PR titles

## Build / Test

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

## Checklist (before considering work done)

- [ ] Is the trade transaction atomic?
- [ ] Does the price cache have a TTL set?
- [ ] Are all recommendation reactions being logged without exception?
- [ ] Are market-specific settings free of hardcoded values?
- [ ] Was the OpenAPI doc updated alongside any API contract change?
