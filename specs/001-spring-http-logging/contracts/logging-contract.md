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

### Response Log Entry

Produced by `HttpResponseLoggingFilter` using plain SLF4J log calls — no custom format strings.

**Format** (plain SLF4J):
```
[HTTP_METHOD] [URI] - [STATUS_CODE] ([ELAPSED]ms)
```

**Examples**:
```
INFO  HttpResponseLoggingFilter : POST /ecommerce/api/v1/auth/login - 200 (143ms)
WARN  HttpResponseLoggingFilter : POST /ecommerce/api/v1/auth/login - 401 (12ms)
ERROR HttpResponseLoggingFilter : GET  /ecommerce/api/v1/orders - 500 (3ms)
```

**Guaranteed fields**: Method, URI, status code, elapsed time.
**Log logger name**: `com.app.ecommerce.logging.HttpResponseLoggingFilter`

---

### Log Level Contract

Applications and monitoring systems MAY use these log levels to filter operational signals:

| Level | Trigger | Action expected |
|---|---|---|
| DEBUG | All 1xx, 3xx + request entries | Development / trace only |
| INFO | 2xx responses | Normal operation |
| WARN | 4xx responses | Client-side issues; may need attention |
| ERROR | 5xx responses | Server-side failures; requires investigation |

---

### Security Guarantee

The `Authorization` header value (JWT Bearer token) MUST NOT appear in any log entry
produced by `CommonsRequestLoggingFilter` or `HttpResponseLoggingFilter`.

Verified by: setting `setIncludeHeaders(false)` on `CommonsRequestLoggingFilter`.
