<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.1 → 1.0.2 (PATCH — Principle VI updated: HttpResponseLoggingFilter removed; response status logging out of scope)

Modified principles:
  - Principle VI (Observability): removed HttpResponseLoggingFilter reference; logging/ package now empty

Added sections: N/A

Removed sections: N/A

Templates requiring updates:
  ✅ CLAUDE.md — logging/ package comment and Recent Changes updated
  ✅ specs/001-spring-http-logging/tasks.md — Phase 4 (HttpResponseLoggingFilter) removed
  ✅ specs/001-spring-http-logging/quickstart.md — Steps 3/4 (response WARN/INFO) removed
  ✅ specs/001-spring-http-logging/contracts/logging-contract.md — Response section removed
  ✅ specs/001-spring-http-logging/research.md — Decisions 2/4/6 updated

Deferred TODOs:
  - /products/** public access: Current SecurityConfiguration.java does NOT whitelist
    /products/**. The user's prompt mentioned it as desired. A follow-up task SHOULD be
    created to evaluate whether read-only product browsing should be open (unauthenticated)
    and update SecurityConfiguration accordingly.
-->

# Ecommerce API Constitution

## Core Principles

### I. Layered Architecture (NON-NEGOTIABLE)

Every feature MUST follow the three-tier separation:

- **Controller layer** (`com.app.ecommerce.controller`) handles HTTP concerns only:
  request parsing, response shaping, and HTTP status codes.
- **Service layer** (`com.app.ecommerce.service`) owns all business logic.
  Controllers MUST NOT contain business logic; services MUST NOT reference
  HTTP types.
- **Repository layer** (`com.app.ecommerce.repository`) is the sole point of
  database access. Services MUST NOT construct JPQL/SQL directly outside
  repository interfaces.

Cross-cutting concerns (logging, timing, validation) MUST be handled via AOP
(`com.app.ecommerce.config.AspectConfiguration`) rather than scattered inline.

### II. DTO-First Communication

All data crossing a public API boundary (request bodies, response bodies) MUST
use Data Transfer Objects defined in `com.app.ecommerce.dtos` and
`com.app.ecommerce.models.request` / `com.app.ecommerce.models.response`.

- JPA `@Entity` objects MUST NOT be serialized directly to or from HTTP responses.
- Entity ↔ DTO conversion MUST use MapStruct mappers in
  `com.app.ecommerce.mappers`; manual field-by-field copying is prohibited.
- Request bodies MUST be validated with Jakarta Bean Validation annotations
  before reaching service methods.

### III. JWT Stateless Authentication (NON-NEGOTIABLE)

The API is stateless. No server-side HTTP session state is permitted
(`SessionCreationPolicy.STATELESS` is mandatory and MUST NOT be changed).

- Every protected endpoint MUST require a valid JWT Bearer token in the
  `Authorization` header.
- Token validation MUST pass through `JwtAuthenticationFilter` (one-per-request,
  `OncePerRequestFilter`) before the request reaches any controller.
- Tokens MUST be cross-checked against the `token` table (`TokenRepo`) to
  reject revoked or expired tokens even before JWT expiry.
- Account enablement (`UserDetails.isEnabled()`) MUST be verified on every
  authenticated request; unverified (not email-confirmed) accounts MUST be
  rejected.
- Password storage MUST use `BCryptPasswordEncoder`; plaintext or reversible
  encoding is prohibited.

**Whitelisted endpoints** (no JWT required):

| Pattern | Purpose |
|---|---|
| `POST /auth/register` | New account creation |
| `POST /auth/login` | Credential exchange for tokens |
| `POST /auth/refresh-token` | Silent token refresh |
| `GET /auth/verify-registration/**` | Email verification callback |
| `GET /auth/forget-password/**` | Forget-password initiation |
| `POST /auth/reset-password` | Password reset confirmation |
| `/swagger-ui/**`, `/api-docs/**`, `/webjars/**` | API documentation |

> **TODO(PRODUCTS_WHITELIST)**: The project prompt specifies `/products/**` as a
> desired public route (unauthenticated product browsing). The current
> `SecurityConfiguration.java` does NOT whitelist it. A decision MUST be made and
> `SecurityConfiguration` updated accordingly before the next minor amendment.

### IV. Interface-Driven Design

Every service and controller implementation MUST implement a corresponding
interface:

- `IXxxService` in `com.app.ecommerce.service.framework`
- `IXxxController` in `com.app.ecommerce.controller.framework`

This ensures testability, mockability, and allows alternative implementations
without breaking callers. Concrete classes live under `.impl` sub-packages.

### V. Async Messaging for Side Effects

Operations that trigger side effects (email notifications, inventory updates)
MUST be decoupled via Apache ActiveMQ queues rather than executed synchronously
in the request thread.

- Queue senders MUST extend or delegate to `DefaultQueueSender`.
- Queue message models live in `com.app.ecommerce.mq.activemq.model`.
- Listeners (`EmailQueueListener`, `ForgetPasswordQueueListener`) MUST be the
  sole consumers of their respective queues.

Synchronous email dispatch inside a request-handling service method is
prohibited.

### VI. Observability

All significant application events MUST be logged using SLF4J (via Lombok `@Slf4j`).

- HTTP **request** logging MUST use Spring's built-in `CommonsRequestLoggingFilter`
  configured as a `@Bean` in `HttpLoggingConfiguration`. Activation is controlled by
  `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG`.
- HTTP **response** status-code logging is out of scope. No `HttpResponseLoggingFilter`
  or any custom response-logging class MUST be created. The `com.app.ecommerce.logging`
  package MUST remain empty after the 001-spring-http-logging migration.
- The `Authorization` header value MUST NOT appear in any log line.
- SQL statement logging is provided by `datasource-proxy-spring-boot-starter`
  and MUST remain enabled in non-production profiles.
- New services MUST NOT introduce raw `System.out.println`; use SLF4J via `@Slf4j`.

## Technology Stack

| Concern | Choice | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.0.0 |
| Build & Dependency Management | Maven | 3.x (spring-boot-maven-plugin) |
| ORM / Data Access | Spring Data JPA + Hibernate | Spring Boot managed |
| Database | MySQL | runtime (mysql-connector-java) |
| Security | Spring Security + JJWT | 0.11.5 |
| Messaging | Apache ActiveMQ (Classic) | 5.18.1 |
| DTO Mapping | MapStruct | 1.6.0 |
| Email | Spring Boot Mail | Spring Boot managed |
| API Documentation | SpringDoc OpenAPI (Swagger UI) | 2.0.2 |
| Boilerplate Reduction | Lombok | Spring Boot managed |
| Templating (email) | Thymeleaf | Spring Boot managed |
| AOP | Spring AOP | Spring Boot managed |

Dependency upgrades that change major versions MUST be treated as a MAJOR
constitution amendment and require a migration plan.

## Package & Directory Conventions

The repository follows a **single-module Maven mono-repo** layout. All source
lives under one Maven artifact (`com.app:EcommerceApp`).

```
src/main/java/com/app/ecommerce/
├── config/          — Spring @Configuration classes (Security, JPA, AOP, Email, MQ, OpenAPI)
├── controller/
│   ├── framework/   — IXxxController interfaces (OpenAPI annotations here)
│   └── impl/        — Concrete @RestController implementations
├── dtos/            — DTO classes for API boundary data
├── email/
│   ├── model/       — Email message detail POJOs
│   └── service/     — Email sending services
├── entity/          — JPA @Entity classes (MUST NOT leave this package boundary via API)
├── enums/           — Shared enum types
├── exception/
│   ├── handler/     — @RestControllerAdvice global handler
│   └── type/        — Custom exception classes
├── factory/         — Object factory helpers
├── logging/         — (empty; reserved for future logging filters)
├── mappers/         — MapStruct @Mapper interfaces
├── models/
│   ├── request/     — Inbound DTO request bodies
│   └── response/    — Outbound DTO response bodies
├── mq/activemq/
│   ├── listener/    — @JmsListener queue consumers
│   ├── model/       — Queue message POJOs
│   └── sender/      — Queue producer services
├── repository/      — Spring Data JPA @Repository interfaces
├── security/
│   ├── filters/     — OncePerRequestFilter implementations (JWT)
│   └── handler/     — Access denied / auth entry point handlers
└── service/
    ├── framework/   — IXxxService interfaces
    └── impl/        — Concrete @Service implementations

src/main/resources/
├── application.yml  — Environment-specific configuration
└── templates/       — Thymeleaf email templates

src/test/java/com/app/ecommerce/
└── ...              — Unit and integration tests mirroring main structure
```

New packages MUST be discussed and documented before introduction; ad-hoc
packages outside this layout are prohibited without a constitution amendment.

## Governance

This constitution supersedes all other conventions, READMEs, and ad-hoc
agreements. When conflicts arise, the constitution wins.

**Amendment Procedure**:
1. Open a PR with the proposed change to `.specify/memory/constitution.md`.
2. State the version bump type (MAJOR / MINOR / PATCH) and rationale.
3. Update all dependent templates identified in the Sync Impact Report header.
4. Obtain review approval before merging.

**Versioning Policy**:
- **MAJOR** (X.0.0): Removal or incompatible redefinition of a principle.
- **MINOR** (x.Y.0): New principle or section; materially expanded guidance.
- **PATCH** (x.y.Z): Clarifications, wording fixes, non-semantic refinements.

**Compliance Review**:
- All PRs MUST include a "Constitution Check" confirming no principles are
  violated, or document a justified exception in the PR body.
- Added complexity beyond what a principle permits MUST be entered in the
  plan.md Complexity Tracking table with rationale.

**Runtime Development Guidance**: Refer to `.specify/templates/agent-file-template.md`
for per-feature agent context generation; keep it in sync with any stack changes.

---

**Version**: 1.0.2 | **Ratified**: 2023-04-17 | **Last Amended**: 2026-03-10
