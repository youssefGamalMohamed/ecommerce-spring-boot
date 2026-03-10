# Feature Specification: Spring Built-in HTTP Request/Response Logging

**Feature Branch**: `001-spring-http-logging`
**Created**: 2026-03-10
**Status**: Draft
**Input**: User description: "i want to remove the current implementation for logging request/response and use built in features of spring boot 2026 by using bean implementation to allow log request and response for all of senarios and also for logging 2xx , 4xx , 5xx and all of status codes"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Full Request Logging via Bean Configuration (Priority: P1)

A developer running the application can see complete incoming HTTP request details
in the application log — method, URI, query parameters, headers, and body — without
any custom filter or utility class. The logging is activated by declaring a single
`@Bean` in a configuration class; no hand-written `OncePerRequestFilter` is required.

**Why this priority**: This is the core replacement. The existing `AppLogger`,
`HttpRequestResponseInterceptorUtils`, and `LoggingUtils` classes are deleted and
every capability they provided for request-side logging MUST be covered by the
new approach before anything else is done.

**Independent Test**: Start the application and send any HTTP request (e.g.,
`POST /auth/login`). The log output MUST show the request method, URI, headers,
query parameters, and body without any of the deleted custom classes being present
in the codebase.

**Acceptance Scenarios**:

1. **Given** the application is running and the three custom logging classes have been
   deleted, **When** any HTTP request is sent to any endpoint, **Then** the application
   log shows the request method, full URI, relevant headers, query parameters, and
   request body (where present).

2. **Given** the application is running, **When** a request is sent with no body
   (e.g., a `GET` request), **Then** the log records the method, URI, and headers
   without errors or blank body entries.

3. **Given** the configuration exists as a `@Bean` declaration only, **When** the
   application starts, **Then** no custom servlet filter or AOP class in the
   `com.app.ecommerce.logging` package performs request logging.

---

### User Story 2 - Removal of Legacy Logging Classes (Priority: P2)

The three custom logging source files (`AppLogger.java`,
`HttpRequestResponseInterceptorUtils.java`, `LoggingUtils.java`) are fully deleted
from the codebase, and no other class in the project references them.

**Why this priority**: Dead code and replaced classes MUST be removed to prevent
confusion, duplicate logging, and maintenance burden. This is the cleanup
complement to P1.

**Independent Test**: A search across the entire repository for references to
`AppLogger`, `HttpRequestResponseInterceptorUtils`, and `LoggingUtils` returns zero
results. The application compiles and starts without errors.

**Acceptance Scenarios**:

1. **Given** the new bean-based logging is in place, **When** the source files for the
   three legacy classes are deleted, **Then** the project compiles successfully with
   no unresolved references.

2. **Given** the legacy files are deleted, **When** the application starts and handles
   requests, **Then** no `ClassNotFoundException` or `NoSuchBeanDefinitionException`
   is thrown.

3. **Given** the `@Around` and `@AfterThrowing` AOP advices that resided in `AppLogger`,
   **When** `AppLogger` is deleted, **Then** those advices are gone entirely and no
   equivalent service-level AOP logging exists anywhere in the codebase.

---

### Edge Cases

- What happens when a request body is a multipart file upload? `CommonsRequestLoggingFilter`
  reads only up to `maxPayloadLength` bytes, so binary content is naturally capped and will
  not cause memory issues.
- What happens when a request arrives with no `Content-Type` header? Logging MUST
  still succeed without throwing an exception.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST log every incoming HTTP request including: HTTP method,
  request URI, query parameters, and request body (when present), up to a configurable
  maximum payload length.

- **FR-002**: All request logging MUST be configured via a single Spring `@Bean`
  declaration in a configuration class using `CommonsRequestLoggingFilter`. No
  hand-written `OncePerRequestFilter` or custom utility class is required. Log message
  format MUST use Spring's default `CommonsRequestLoggingFilter` output with no custom
  prefix strings.

- **FR-003**: The `Authorization` header value MUST be omitted from request logs
  (`setIncludeHeaders(false)`) to prevent accidental exposure of JWT tokens.

- **FR-004**: The legacy classes `AppLogger`, `HttpRequestResponseInterceptorUtils`,
  and `LoggingUtils` MUST be deleted from `com.app.ecommerce.logging` and all
  references to them MUST be removed from the codebase. The `@Around` and
  `@AfterThrowing` AOP advices previously in `AppLogger` MUST NOT be migrated to
  any other class; service-level AOP logging is discontinued entirely.

- **FR-005**: The maximum number of characters logged for a request body MUST be
  configurable via `setMaxPayloadLength` on `CommonsRequestLoggingFilter`, with a
  default of 10,000 characters to prevent log bloat on large payloads.

- **FR-006**: The logging implementation MUST NOT use any form of persistence. Log entries
  MUST be written exclusively to the application log output (console/appender). No database
  tables, no in-memory HTTP exchange repositories, and no file-based exchange stores are
  permitted.

- **FR-007**: No `HttpResponseLoggingFilter` or any custom response-logging class MUST
  be created. Response status code logging is out of scope for this feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every HTTP request handled by the application produces a DEBUG log entry
  containing method and URI — verifiable by reviewing application logs after sending
  requests to 5 different endpoints with `CommonsRequestLoggingFilter` at DEBUG level.

- **SC-002**: The three legacy source files (`AppLogger.java`, `HttpRequestResponseInterceptorUtils.java`,
  `LoggingUtils.java`) no longer exist anywhere in the codebase after migration —
  verifiable by running a grep for these class names and confirming empty output.

- **SC-003**: The `com.app.ecommerce.logging` package is empty after migration (no
  `HttpResponseLoggingFilter.java` is created) — verifiable by listing the package directory.

- **SC-004**: The application compiles and starts successfully with zero errors after
  all legacy logging classes are removed — verifiable by a clean build and startup.

- **SC-005**: No JWT token value appears in plain text in application logs when a
  request with an `Authorization: Bearer <token>` header is sent — verifiable by
  inspecting the log output for the literal token string.

## Assumptions

- The `@Around` and `@AfterThrowing` AOP advices inside `AppLogger` (service-level logging)
  are deleted together with `AppLogger`. No service-level AOP logging is retained or migrated.
- Log output format (pattern, appender, file vs. console) is not changed by this
  feature; only the mechanism that captures and emits request/response data changes.
- "Built-in features" means using Spring-provided filter infrastructure configured
  via `@Bean`, avoiding the pattern of custom multi-responsibility utility classes
  that existed before.

## Clarifications

### Session 2026-03-10

- Q: Should the logging feature use any form of persistence (database, file store, in-memory repository)? → A: No persistence of any kind. All logging is write-only to the application log output (console/file appender). No `HttpExchangeRepository`, no database tables, no in-memory exchange store.
- Q: What should happen to the `@Around` and `@AfterThrowing` AOP advices inside `AppLogger` when the class is deleted? → A: Delete both advices entirely. No service-level AOP logging is carried forward.
- Q: What log format should the request filter use? → A: Use Spring's default `CommonsRequestLoggingFilter` output format with no custom prefix strings — no "REQUEST >>" markers.
- Q: Should `HttpResponseLoggingFilter` be created for response/status-code logging? → A: No. `HttpResponseLoggingFilter` MUST NOT be created. Response status logging is out of scope. Only `CommonsRequestLoggingFilter` via `@Bean` is used. The `com.app.ecommerce.logging` package will be empty after migration.
