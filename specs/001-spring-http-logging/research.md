# Research: Spring Built-in HTTP Request/Response Logging

**Feature**: 001-spring-http-logging
**Date**: 2026-03-10

---

## Decision 1: Request Logging Mechanism

**Decision**: Use `org.springframework.web.filter.CommonsRequestLoggingFilter` registered as a `@Bean`

**Rationale**: `CommonsRequestLoggingFilter` is Spring Framework's built-in filter for structured
request logging. It is already included in `spring-boot-starter-web` (no new dependency required).
It natively supports logging: HTTP method, URI, query string, headers, and request body up to a
configurable max length. Activated via a single log-level property.

**Alternatives considered**:
- `AbstractRequestLoggingFilter` (super-class of CommonsRequestLoggingFilter) — more flexible but
  requires subclassing; unnecessary since CommonsRequestLoggingFilter covers all requirements.
- Spring Boot Actuator `HttpExchangeRepository` — stores exchanges in-memory and exposes via
  `/actuator/httpexchanges` endpoint. Useful for dashboards but does NOT emit log lines, so it
  cannot fulfill the "log to application log" requirement. Would also require adding Actuator
  dependency.
- Tomcat access log (`server.tomcat.accesslog.*`) — only logs request metadata, not body or
  response body. Server-level, not application-level.

**Configuration**:
```java
@Bean
public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(10_000);   // configurable cap
    filter.setIncludeHeaders(false);      // Authorization header excluded (masking)
    // No custom prefix — use Spring's default format
    return filter;
}
```

**Log level activation** (add to `application.properties`):
```properties
logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG
```

**Important**: `CommonsRequestLoggingFilter` logs at `DEBUG` level only. Setting the property
above enables it. In production environments where `DEBUG` is suppressed, request logs will be
silenced automatically — which is the correct default security posture.

---

## Decision 2: Response Logging Mechanism

**Decision**: Use a minimal `OncePerRequestFilter` subclass (`HttpResponseLoggingFilter`) registered
as a `FilterRegistrationBean` `@Bean`. This is the only viable way to log HTTP responses in Spring —
there is no built-in response logging filter.

**Rationale**: Spring Framework provides `ContentCachingResponseWrapper` which buffers the response
body so it can be read after the response has been written. A `OncePerRequestFilter` wrapping the
entire filter chain with both a `ContentCachingRequestWrapper` and `ContentCachingResponseWrapper`
is the idiomatic Spring pattern. It is registered as a `@Bean` (via `FilterRegistrationBean`) in
the logging configuration class — satisfying the "bean implementation" requirement.

**Status-aware log levels**:
| Status Range | Log Level | Rationale |
|---|---|---|
| 1xx | DEBUG | Informational, rarely emitted |
| 2xx | INFO | Nominal success |
| 3xx | DEBUG | Redirects, low operational signal |
| 4xx | WARN | Client-side problem, needs monitoring |
| 5xx | ERROR | Server-side failure, requires immediate attention |

**Critical implementation note**: `responseWrapper.copyBodyToResponse()` MUST be called in a
`finally` block after the filter chain completes; otherwise the response body is never written to
the client (cached but not flushed).

**Alternatives considered**:
- Spring Actuator `HttpExchangeRepository` — see Decision 1; does not emit log lines.
- Using a `HandlerInterceptor` (`postHandle` / `afterCompletion`) — cannot reliably access response
  body bytes via `HandlerInterceptorAdapter`.

---

## Decision 3: Header Masking (Authorization)

**Decision**: Disable header logging entirely in `CommonsRequestLoggingFilter`
(`setIncludeHeaders(false)`).

**Rationale**: The `Authorization: Bearer <JWT>` header contains the active token. Logging it
plaintext creates a security risk (any log aggregation system or log file would contain live tokens).
Spring Boot 3.x's `CommonsRequestLoggingFilter` does not have a per-header masking predicate; the
simplest and safest option is to suppress header logging in the request filter. The HTTP method and
URI provide sufficient context for debugging without requiring headers.

**Alternatives considered**:
- Implementing a custom `setHeaderPredicate` — available in Spring 5.3+ but still requires knowing
  the header names; any missed sensitive header is a security leak.
- Log headers but redact `Authorization` — fragile; relies on developer discipline.

---

## Decision 4: Filter Ordering with Spring Security

**Decision**: Register the response logging filter at `SecurityProperties.DEFAULT_FILTER_ORDER - 1`
(one position before Spring Security's `FilterChainProxy`).

**Rationale**: To log every HTTP response including those rejected by Spring Security (e.g., 401
Unauthorized, 403 Forbidden), the response logging filter MUST wrap the entire Spring Security
filter chain. If the filter runs at a lower priority (higher order number), security rejections
happen inside the chain before the wrapper captures the response. Using
`SecurityProperties.DEFAULT_FILTER_ORDER - 1` mirrors the ordering the removed `AppLogger` used.

**Outcome**: The filter execution order will be:
```
HttpResponseLoggingFilter (order: DEFAULT_FILTER_ORDER - 1)
  └── Spring Security FilterChainProxy (order: DEFAULT_FILTER_ORDER)
        └── JwtAuthenticationFilter
              └── Controller / Exception handlers
```

This ensures 401/403 responses from Spring Security are also logged at WARN level.

---

## Decision 5: pom.xml Changes

**Decision**: No dependency changes required.

**Rationale**: `CommonsRequestLoggingFilter` is part of `spring-web` which is already included via
`spring-boot-starter-web`. `ContentCachingRequestWrapper` and `ContentCachingResponseWrapper` are
also in `spring-web`. Spring Boot 3.x uses SLF4J + Logback by default (no commons-logging jar
needed at runtime).

**Spring Boot parent version**: The current project uses `spring-boot-starter-parent 3.0.0`. No
upgrade is needed for this feature — all required classes exist in 3.0.0.

---

## Decision 6: File Structure Impact

**Files to delete**:
- `src/main/java/com/app/ecommerce/logging/AppLogger.java`
- `src/main/java/com/app/ecommerce/logging/HttpRequestResponseInterceptorUtils.java`
- `src/main/java/com/app/ecommerce/logging/LoggingUtils.java`

**Files to create**:
- `src/main/java/com/app/ecommerce/config/HttpLoggingConfiguration.java`
  — `@Configuration` class containing the `CommonsRequestLoggingFilter` bean and the
  `FilterRegistrationBean<HttpResponseLoggingFilter>` bean.
- `src/main/java/com/app/ecommerce/logging/HttpResponseLoggingFilter.java`
  — Minimal `OncePerRequestFilter` for status-aware response logging.

**Files to modify**:
- `src/main/resources/application.properties`
  — Add `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG`
- `.specify/memory/constitution.md`
  — Amend Principle VI (Observability): remove references to deleted classes.

**AOP aspects in `AppLogger`** (clarified decision):
The `@Around` and `@AfterThrowing` advices inside `AppLogger` that log service method inputs and
outputs are **deleted entirely** along with `AppLogger`. No migration to a `ServiceLoggingAspect`
or any other class. Service-level AOP logging is discontinued with no replacement.

---

## Decision 7: Max Body Size Configuration

**Decision**: Default to `10,000` characters (≈10 KB) for request body. Response body: read up to
`10,000` bytes; truncate with `[TRUNCATED - X bytes total]` if exceeded.

**Rationale**: Prevents log bloat on large product list responses or file upload requests.
This value is set in code; future iteration can expose it via `@ConfigurationProperties`.
