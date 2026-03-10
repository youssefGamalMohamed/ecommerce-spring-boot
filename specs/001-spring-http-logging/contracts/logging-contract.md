# Contract: HTTP Logging Behaviour

**Feature**: 001-spring-http-logging
**Date**: 2026-03-10

This feature introduces no new API endpoints and changes no existing API contracts.
It is a purely internal infrastructure change.

---

## Observable Behaviour Contract (Log Output)

The following describes the observable contract that consumers of application logs can depend on.

### Request Log Entry

Produced by `CommonsRequestLoggingFilter` at `DEBUG` level using Spring's default format.
No custom prefix strings are set.

**Format** (Spring default `CommonsRequestLoggingFilter` output):
```
After request [POST /ecommerce/api/v1/auth/login, payload=[{"email":"...","password":"..."}]]
```

**Guaranteed fields**: HTTP method, URI, query string (when present), payload (when present).
**Excluded fields**: Headers (all headers suppressed; `setIncludeHeaders(false)`).

**Log logger name**: `org.springframework.web.filter.CommonsRequestLoggingFilter`
**Log level**: `DEBUG`

---

### Out of Scope

Response status-code logging (`HttpResponseLoggingFilter`) is **not part of this feature**.
No log entries for HTTP response status codes (2xx/4xx/5xx) are produced.

---

### Security Guarantee

The `Authorization` header value (JWT Bearer token) MUST NOT appear in any log entry
produced by `CommonsRequestLoggingFilter`.

Verified by: setting `setIncludeHeaders(false)` on `CommonsRequestLoggingFilter`.
