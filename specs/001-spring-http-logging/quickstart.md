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

# Should return empty directory listing
ls src/main/java/com/app/ecommerce/logging/
# Expected: only HttpResponseLoggingFilter.java
```

---

## Step 2: Verify Request Logging (2xx)

Send a valid login request:

```bash
curl -s -X POST http://localhost:8081/ecommerce/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234!"}'
```

**Expected in application log**:
```
DEBUG CommonsRequestLoggingFilter : After request [POST /ecommerce/api/v1/auth/login, payload=[{...}]]
INFO  HttpResponseLoggingFilter   : POST /ecommerce/api/v1/auth/login - 200 (...ms)
```

---

## Step 3: Verify 4xx Logging (WARN)

Send a request with invalid credentials:

```bash
curl -s -X POST http://localhost:8081/ecommerce/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"wrong@example.com","password":"wrong"}'
```

**Expected in application log**:
```
DEBUG CommonsRequestLoggingFilter : After request [POST /ecommerce/api/v1/auth/login, payload=[{...}]]
WARN  HttpResponseLoggingFilter   : POST /ecommerce/api/v1/auth/login - 401 (...ms)
```

---

## Step 4: Verify 4xx for Unauthenticated Access (Spring Security 401)

Access a protected endpoint without a token:

```bash
curl -s -X GET http://localhost:8081/ecommerce/api/v1/orders
```

**Expected in application log**:
```
DEBUG CommonsRequestLoggingFilter : After request [GET /ecommerce/api/v1/orders]
WARN  HttpResponseLoggingFilter   : GET /ecommerce/api/v1/orders - 401 (...ms)
```

This confirms the logging filter wraps Spring Security correctly.

---

## Step 5: Verify Authorization Header Is NOT Logged

Send a request with an Authorization token and verify the token value does NOT appear:

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9.test"
curl -s -X GET http://localhost:8081/ecommerce/api/v1/orders \
  -H "Authorization: Bearer $TOKEN"
```

**Verify in application log**: search for the literal token value — it MUST NOT appear.

```bash
# This grep should return NO output
grep "$TOKEN" application.log
```

---

## Step 6: Verify Body Truncation

If you have an endpoint returning a large list, confirm truncation:

```bash
curl -s -X GET http://localhost:8081/ecommerce/api/v1/products \
  -H "Authorization: Bearer <valid-token>"
```

**Expected**: If response body exceeds 10,000 characters, log shows `[TRUNCATED - XXXXX bytes total]`
instead of the full body.

---

## Validation Checklist

- [ ] No references to `AppLogger`, `HttpRequestResponseInterceptorUtils`, `LoggingUtils` in source
- [ ] No `@Around` or `@AfterThrowing` service-logging aspects exist anywhere in codebase
- [ ] No HTTP exchange persistence (no `HttpExchangeRepository`, no exchange DB table)
- [ ] Application starts without errors
- [ ] 2xx responses logged at INFO
- [ ] 4xx responses logged at WARN (including Spring Security rejections)
- [ ] 5xx responses logged at ERROR
- [ ] `Authorization` header value not visible in any log line
- [ ] Large response bodies are truncated, not fully dumped
