# AGENTS.md

Guide for any AI coding agent working in the Spring Boot backend repo.

## Project Context

Main backend for an AI mock investment battle platform. This repo owns **domain logic where correctness matters** — trading, challenges, portfolio. Trading decisions and recommendation generation belong to a separate FastAPI AI service; this repo only consumes those results.

- This is not a real trading service (education positioning). Do not propose or implement features that monetize or encourage trading frequency.

## Stack

Kotlin, Spring Boot, Spring Data JPA, Spring Security, JWT, PostgreSQL, Redis, Gradle (Kotlin DSL)

## Domain Modules

`user`, `market`, `challenge`, `trading`, `portfolio`, `recommendation` (reaction logging), `learning`

Packages are organized by **domain, not by layer**. Before adding new code, decide which domain it belongs to and place it in that package.

## Boundary with the FastAPI AI Service (important)

- Do not implement trading decision or recommendation generation logic in this repo. Call the FastAPI service over HTTP and only process the result (action, recommended symbol, probability).
- Every response received from FastAPI must be paired with a **decision log write**. Without this log, the observability system and retraining pipeline cannot function.
- If the API changes, update the OpenAPI spec in the same commit. Web and iOS clients consume this repo's API directly.

## Market Handling Principles

- Model market type as a 3-way enum: `KR` (domestic), `US`, `COIN` — always designed with all three in mind.
- Only `US` and `COIN` are currently active. `KR` is disabled due to a pending brokerage account requirement, but its code path should remain behind a feature flag rather than being removed.
- Trading hours, currency unit, and price limit rules must never be hardcoded — always market-specific config.

## Transaction / Data Integrity Rules

- Trade execution (`order → execution → balance update`) must be wrapped in a **single transaction**. No partial failures allowed.
- Challenge settlement (win/loss determination, final return calculation) must also be transactional.
- Price lookups: Redis cache first, external API only on a cache miss. Never build a cache without a TTL.
- User reactions to AI recommendations (accept/reject/ignore) must always be logged, without exception.

## Coding Conventions

- Commit message format: `feat :: description`
- No prefixes on PR titles
- JPA entities are the only classes subject to `allOpen` (auto-applied by `plugin.jpa`): `@Entity`, `@MappedSuperclass`, `@Embeddable`

## Build & Run

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

## Do Not (checklist)

- [ ] Do not implement trading decision or recommendation generation logic directly in this repo (that's the FastAPI service's responsibility)
- [ ] Do not make trade count or recommendation frequency a paywall trigger
- [ ] Do not consider AI response handling logic complete without a decision log write
- [ ] Do not hardcode market-specific settings (trading hours, currency, price limits)
- [ ] Do not create a price cache without a TTL
- [ ] Do not change the API contract without updating the OpenAPI docs
