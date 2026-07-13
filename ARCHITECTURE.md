# Architecture

## Package Layout

```text
team.cklob.arena
├── domain
│   └── {domainName}
│       ├── presentation
│       │   ├── request
│       │   ├── response
│       │   └── controller
│       ├── application
│       │   └── impl
│       ├── domain
│       │   ├── entity
│       │   ├── type
│       │   └── repository
│       └── infrastructure
└── global
    ├── common
    ├── config
    ├── security
    ├── exception
    ├── response
    ├── annotation
    ├── util
    └── property
```

`{domainName}` is one of `user`, `market`, `challenge`, `trading`, `portfolio`, `recommendation`, or `learning`.

Do not create an empty package. Add a package only when it contains production code.

## Domain Layers

- `presentation`: HTTP controllers and request/response DTOs. Controllers validate input and delegate only to application services.
- `application`: use cases, orchestration, and transaction boundaries. Place concrete service implementations in `application.impl`.
- `domain`: entities, domain types, and repository interfaces. Keep domain business rules here when they do not require orchestration or external I/O.
- `infrastructure`: external API clients, persistence adapters, and other technology-specific implementations.

Use the package form `team.cklob.arena.domain.{domainName}.{layer}`. A domain may depend on `global`, but one domain must not reach into another domain's `presentation` or `application` package.

## Global Packages

- `common`: cross-cutting types that do not belong to another global concern.
- `config`: Spring configuration and framework integration setup.
- `security`: JWT, authentication, authorization, and Spring Security components.
- `exception`: shared exception contracts, error-code contracts, and global exception handlers.
- `response`: shared API response envelopes and response advice.
- `annotation`: reusable annotations and their supporting code.
- `util`: stateless helpers with no domain ownership.
- `property`: `@ConfigurationProperties` classes.

`global` must not contain domain business logic, domain entities, or domain repositories.
