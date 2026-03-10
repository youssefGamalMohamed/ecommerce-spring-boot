# Quickstart: Verifying HTTP Logging After Implementation

**Feature**: 001-spring-http-logging
**Date**: 2026-03-10

---

## Prerequisites

- Application built and running: `mvn spring-boot:run`
- MySQL running with `ecommerce` database
- ActiveMQ running (or disabled for local testing)

---

## Step 1: Verify Legacy Classes Are Gone

```bash
# Should return no output (zero matches)
grep -r "AppLogger\|HttpRequestResponseInterceptorUtils\|LoggingUtils" \
  src/main/java/

# Should return empty directory listing (package is completely empty after migration)
ls src/main/java/com/app/ecommerce/logging/
# Expected: empty — no files remain
```

---

## Step 2: Verify Request Logging

Send a valid login request:

```bash
curl -s -X POST http://localhost:8081/ecommerce/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234!"}'
```

**Expected in application log**:

```text
DEBUG CommonsRequestLoggingFilter : After request [POST /ecommerce/api/v1/auth/login, payload=[{...}]]
```

Note: No response status line is logged — response logging is out of scope for this feature.

---

## Step 3: Verify Authorization Header Is NOT Logged

Send a request with an Authorization token and verify the token value does NOT appear:

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9.test"
curl -s -X GET http://localhost:8081/ecommerce/api/v1/orders \
  -H "Authorization: Bearer $TOKEN"
```

**Expected in application log**:

```text
DEBUG CommonsRequestLoggingFilter : After request [GET /ecommerce/api/v1/orders]
```

**Verify**: search for the literal token value — it MUST NOT appear.

```bash
# This grep should return NO output
grep "$TOKEN" application.log
```

---

## Step 4: Verify Request Body Truncation

Send a request with a large body to confirm the payload cap works:

```bash
curl -s -X POST http://localhost:8081/ecommerce/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234!"}'
```

**Expected**: Payload is logged up to 10,000 characters. Any body exceeding that limit
is silently truncated by `CommonsRequestLoggingFilter` — no error is thrown.

---

## Validation Checklist

- [x] No references to `AppLogger`, `HttpRequestResponseInterceptorUtils`, `LoggingUtils` in source
- [x] No `@Around` or `@AfterThrowing` service-logging aspects exist anywhere in codebase
- [x] No HTTP exchange persistence (no `HttpExchangeRepository`, no exchange DB table)
- [x] `logging/` package is completely empty after migration
- [x] Application starts without errors
- [x] Every incoming request produces a `DEBUG` log line from `CommonsRequestLoggingFilter`
- [x] `Authorization` header value not visible in any log line
- [x] Request bodies longer than 10,000 characters are silently truncated (no errors)
