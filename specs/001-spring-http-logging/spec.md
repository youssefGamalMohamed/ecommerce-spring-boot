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

### User Story 2 - HTTP Status-Aware Response Logging (Priority: P2)

A developer or operator can see, for every HTTP response, the status code, body,
and response time — with the log level or label clearly distinguishing successful
responses (2xx), client errors (4xx), and server errors (5xx). This enables fast
triage of incidents without parsing raw status numbers in generic `INFO` lines.

**Why this priority**: The existing implementation logs all responses at the same
`INFO` level without differentiating by status category. Differentiating log
severity or label by status family (2xx / 4xx / 5xx) gives operators actionable
signal at a glance.

**Independent Test**: Send three requests:
- One that succeeds (e.g., valid login → 200)
- One that causes a client error (e.g., invalid credentials → 401)
- One that causes a server error (simulate or trigger a 500)

The log output for each MUST visually distinguish the status family (e.g., via
log level: `INFO` for 2xx, `WARN` for 4xx, `ERROR` for 5xx), and MUST include
the status code, response body, and elapsed time.

**Acceptance Scenarios**:

1. **Given** an endpoint returns a `2xx` response, **When** the response is logged,
   **Then** it is recorded at informational level and includes the status code, body,
   and elapsed time in milliseconds.

2. **Given** an endpoint returns a `4xx` response (e.g., 400, 401, 403, 404),
   **When** the response is logged, **Then** it is recorded at warning level (or
   labeled as a client error) and includes the status code, body, and elapsed time.

3. **Given** an endpoint returns a `5xx` response, **When** the response is logged,
   **Then** it is recorded at error level and includes the status code, body, and
   elapsed time.

4. **Given** any status code response, **When** the response body is empty (e.g.,
   `204 No Content`), **Then** the log entry omits the body field rather than
   logging a blank or null value.

---

### User Story 3 - Removal of Legacy Logging Classes (Priority: P3)

The three custom logging source files (`AppLogger.java`,
`HttpRequestResponseInterceptorUtils.java`, `LoggingUtils.java`) are fully deleted
from the codebase, and no other class in the project references them.

**Why this priority**: Dead code and replaced classes MUST be removed to prevent
confusion, duplicate logging, and maintenance burden. This is the cleanup
complement to P1 and P2.

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

- What happens when a request body is a multipart file upload? Binary content MUST
  be skipped or replaced with a placeholder in the log rather than buffering the
  entire file into memory.
- What happens when the response body is very large (e.g., a large product list)?
  A configurable maximum body length MUST be applied; content beyond the limit MUST
  be truncated with an indicator (e.g., `[truncated]`).
- What happens when an exception causes no response body to be written? The logger
  MUST still record the status code and elapsed time.
- What happens when a request arrives with no `Content-Type` header? Logging MUST
  still succeed without throwing an exception.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST log every incoming HTTP request including: HTTP method,
  request URI, query parameters, request headers (excluding sensitive values such as
  the raw `Authorization` header value), and request body (when present and not binary).

- **FR-002**: The system MUST log every outgoing HTTP response including: HTTP status
  code, response body (up to a configurable maximum length), and elapsed processing
  time in milliseconds.

- **FR-003**: Response log entries MUST use differentiated log severity based on the
  HTTP status code family: informational/success level for 2xx, warning level for
  4xx, and error level for 5xx responses.

- **FR-004**: All request and response logging MUST be configured via Spring `@Bean`
  declarations in a configuration class, with no hand-written custom utility classes
  required for the core logging behaviour. Log message format MUST use Spring's
  default `CommonsRequestLoggingFilter` output with no custom prefix strings (no
  "REQUEST >>" or "RESPONSE <<"). The `HttpResponseLoggingFilter` MUST emit plain
  SLF4J log statements at the appropriate level without custom format wrappers.

- **FR-005**: The `Authorization` header value MUST be masked or omitted from request
  logs to prevent accidental exposure of JWT tokens in log output.

- **FR-006**: The legacy classes `AppLogger`, `HttpRequestResponseInterceptorUtils`,
  and `LoggingUtils` MUST be deleted from `com.app.ecommerce.logging` and all
  references to them MUST be removed from the codebase. The `@Around` and
  `@AfterThrowing` AOP advices previously in `AppLogger` MUST NOT be migrated to
  any other class; service-level AOP logging is discontinued entirely.

- **FR-007**: The logging mechanism MUST handle all HTTP response status codes
  (100–599) without throwing an exception; any unrecognised or unusual status code
  MUST fall back to informational-level logging.

- **FR-008**: Response body logging for binary content types (e.g., images, multipart)
  MUST be replaced with a placeholder string (e.g., `[binary content]`) rather than
  logging raw bytes.

- **FR-009**: The maximum number of characters logged for a single request or response
  body MUST be configurable via application properties, with a default that prevents
  log bloat on large payloads.

- **FR-010**: The logging implementation MUST NOT use any form of persistence. Log entries
  MUST be written exclusively to the application log output (console/appender). No database
  tables, no in-memory HTTP exchange repositories, and no file-based exchange stores are
  permitted.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every HTTP request handled by the application produces a log entry
  containing method, URI, and status code — verifiable by reviewing application logs
  after sending requests to 5 different endpoints.

- **SC-002**: Log entries for 4xx and 5xx responses are visually distinguishable from
  2xx entries without parsing the status code number — verifiable by reading raw log
  output and confirming different severity labels or keywords appear.

- **SC-003**: The three legacy source files (`AppLogger.java`, `HttpRequestResponseInterceptorUtils.java`,
  `LoggingUtils.java`) no longer exist in `com.app.ecommerce.logging` after migration —
  verifiable by confirming only `HttpResponseLoggingFilter.java` remains in the package.

- **SC-004**: The application compiles and starts successfully with zero errors after
  all legacy logging classes are removed — verifiable by a clean build and startup.

- **SC-005**: No JWT token value appears in plain text in application logs when a
  request with an `Authorization: Bearer <token>` header is logged — verifiable by
  inspecting the log output for the literal token string.

- **SC-006**: Sending a request that returns a response body larger than the configured
  maximum produces a truncated log entry with a truncation indicator — verifiable by
  sending a large-payload request and checking the log.

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
- Q: What log format should the request and response filters use? → A: Use Spring's default `CommonsRequestLoggingFilter` output format with no custom prefix strings. The `HttpResponseLoggingFilter` MUST use plain SLF4J log calls — no custom "REQUEST >>" or "RESPONSE <<" format strings.
