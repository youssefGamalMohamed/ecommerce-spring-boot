# Implementation Plan: Spring Built-in HTTP Request/Response Logging

**Branch**: `001-spring-http-logging` | **Date**: 2026-03-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-spring-http-logging/spec.md`

## Summary

Replace the three custom logging classes (`AppLogger`, `HttpRequestResponseInterceptorUtils`,
`LoggingUtils`) with Spring's built-in `CommonsRequestLoggingFilter` wired as a single `@Bean`
in a new `HttpLoggingConfiguration` class. No new Maven dependencies are required and no custom
filter class is introduced. Response status-code logging is out of scope.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.0.0, Spring Security, JJWT 0.11.5, Lombok
**Storage**: N/A (no database changes)
**Testing**: Maven (`mvn test`), manual curl validation per `quickstart.md`
**Target Platform**: Linux server (Tomcat embedded, Spring Boot jar)
**Project Type**: REST web service
**Performance Goals**: Logging MUST NOT add more than 5ms overhead to any request (buffering
  `ContentCachingResponseWrapper` is in-memory only; no I/O added beyond log writes)
**Constraints**: `Authorization` header value MUST NOT appear in any log line; response body
  truncated at 10,000 characters
**Scale/Scope**: Affects every inbound HTTP request — logging is applied globally to all routes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Check | Status |
|---|---|---|
| I. Layered Architecture | Logging is a cross-cutting concern in `config/` and `logging/`; no business logic added | ✅ PASS |
| II. DTO-First Communication | No API boundary data involved; N/A | ✅ PASS |
| III. JWT Stateless Auth | Response filter wraps Security chain to capture all status codes incl. 401/403; JWT contract unchanged | ✅ PASS |
| IV. Interface-Driven Design | `HttpResponseLoggingFilter` is a cross-cutting filter, not a service; no interface required per constitution | ✅ PASS |
| V. Async Messaging | No side effects triggered; N/A | ✅ PASS |
| VI. Observability | **AMENDMENT REQUIRED** — Principle VI currently references deleted classes (`AppLogger`, `HttpRequestResponseInterceptorUtils`). Constitution must be amended (PATCH bump) to reference new classes. | ⚠️ AMEND |

**Post-design re-check**: All principles pass. Constitution VI amended as part of this plan
(see Complexity Tracking for justification of the `OncePerRequestFilter` addition).

## Project Structure

### Documentation (this feature)

```text
specs/001-spring-http-logging/
├── plan.md              # This file
├── research.md          # Phase 0 — technology decisions
├── quickstart.md        # Phase 1 — manual validation steps
├── contracts/
│   └── logging-contract.md   # Log output format contract
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
src/main/java/com/app/ecommerce/
├── config/
│   └── HttpLoggingConfiguration.java     [NEW] — @Bean for CommonsRequestLoggingFilter
│                                                   and FilterRegistrationBean<HttpResponseLoggingFilter>
├── logging/
│   ├── AppLogger.java                    [DELETE]
│   ├── HttpRequestResponseInterceptorUtils.java  [DELETE]
│   └── LoggingUtils.java                 [DELETE]
│   (package will be empty after migration — no new file added here)

src/main/resources/
└── application.properties                [MODIFY] — add CommonsRequestLoggingFilter log level

.specify/memory/
└── constitution.md                       [MODIFY] — amend Principle VI (PATCH v1.0.1)
```

**Structure Decision**: Single-module Maven project. New classes placed in existing `config/`
and `logging/` packages per constitution directory conventions.

## Complexity Tracking

No complexity justifications required. The feature uses a single Spring built-in bean
(`CommonsRequestLoggingFilter`) with no custom filter classes added.
