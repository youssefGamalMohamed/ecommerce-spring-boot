---
description: "Task list for Spring Built-in HTTP Request/Response Logging"
---

# Tasks: Spring Built-in HTTP Request/Response Logging

**Input**: Design documents from `/specs/001-spring-http-logging/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, contracts/logging-contract.md ✅

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story this task belongs to (US1, US2, US3)

## Path Conventions

All source paths are under `src/main/java/com/app/ecommerce/` (single Maven module).

---

## Phase 1: Setup

**Purpose**: No new project structure is needed — all packages already exist.
This phase confirms the working environment before any changes are made.

- [ ] T001 Confirm project compiles on current branch: run `mvn compile` from repo root and verify zero errors before any changes

---

## Phase 2: Foundational — Delete Legacy Logging Classes

**Purpose**: Remove the three legacy classes that will be replaced. These MUST be
deleted before adding new beans to avoid duplicate filter registrations and stale
AOP advice.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T002 [P] Delete `src/main/java/com/app/ecommerce/logging/AppLogger.java` — removes the OncePerRequestFilter + @Aspect bean (includes @Around and @AfterThrowing advices; do NOT migrate them anywhere)
- [ ] T003 [P] Delete `src/main/java/com/app/ecommerce/logging/HttpRequestResponseInterceptorUtils.java`
- [ ] T004 [P] Delete `src/main/java/com/app/ecommerce/logging/LoggingUtils.java`
- [ ] T005 Verify project still compiles after all three deletions: run `mvn compile` and confirm zero errors (no dangling autowired references should remain since these classes were only used by each other)

**Checkpoint**: Foundation ready — three legacy files gone, project compiles cleanly.

---

## Phase 3: User Story 1 — Full Request Logging via Bean (Priority: P1) 🎯 MVP

**Goal**: Incoming HTTP requests are logged using Spring's built-in
`CommonsRequestLoggingFilter`, configured as a `@Bean` with no custom prefix strings.

**Independent Test**: Start the app, send `POST /ecommerce/api/v1/auth/login`, and
confirm the application log contains a `DEBUG` line from
`org.springframework.web.filter.CommonsRequestLoggingFilter` showing the URI and payload.

### Implementation for User Story 1

- [ ] T006 [US1] Create `src/main/java/com/app/ecommerce/config/HttpLoggingConfiguration.java` as a `@Configuration` class containing a `@Bean` method named `requestLoggingFilter()` that returns a `CommonsRequestLoggingFilter` with: `setIncludeQueryString(true)`, `setIncludePayload(true)`, `setMaxPayloadLength(10_000)`, `setIncludeHeaders(false)` — no custom prefix strings
- [ ] T007 [US1] Add the following property to `src/main/resources/application.properties`: `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG`

**Checkpoint**: US1 complete — start the application, send any request, and observe
Spring's default `After request [...]` DEBUG line in the log.

---

## Phase 4: User Story 2 — HTTP Status-Aware Response Logging (Priority: P2)

**Goal**: Every HTTP response is logged at a level matching its status code family
(INFO for 2xx, WARN for 4xx, ERROR for 5xx) using plain SLF4J calls.

**Independent Test**: Send three requests — a valid login (200), bad credentials (401),
and an intentional 500 — and confirm INFO, WARN, and ERROR log lines respectively,
each showing `[METHOD] [URI] - [STATUS] ([ELAPSED]ms)`.

### Implementation for User Story 2

- [ ] T008 [US2] Create `src/main/java/com/app/ecommerce/logging/HttpResponseLoggingFilter.java` extending `OncePerRequestFilter` with `@Slf4j` (Lombok). Implement `doFilterInternal`:
  - Wrap the incoming request with `ContentCachingRequestWrapper`
  - Wrap the response with `ContentCachingResponseWrapper`
  - Record `startTime = System.currentTimeMillis()` before `filterChain.doFilter(...)`
  - In a `finally` block: compute `elapsedMs`, read `status = responseWrapper.getStatus()`
  - Log using plain SLF4J: format `"{} {} - {} ({}ms)"` with method, URI, status, elapsed
  - Log level: `log.info` for 2xx; `log.warn` for 4xx; `log.error` for 5xx; `log.debug` for 1xx and 3xx
  - Call `responseWrapper.copyBodyToResponse()` as the LAST statement in the `finally` block
- [ ] T009 [US2] Add a `FilterRegistrationBean<HttpResponseLoggingFilter>` `@Bean` method to `src/main/java/com/app/ecommerce/config/HttpLoggingConfiguration.java`. Set order to `SecurityProperties.DEFAULT_FILTER_ORDER - 1` so the filter wraps the entire Spring Security chain and captures 401/403 rejections
- [ ] T010 [US2] Add binary content guard to `HttpResponseLoggingFilter.doFilterInternal` in `src/main/java/com/app/ecommerce/logging/HttpResponseLoggingFilter.java`: check `response.getContentType()` — if it starts with `image/`, `video/`, `audio/`, or `application/octet-stream`, skip body logging entirely (the `[METHOD] [URI] - [STATUS] ([ELAPSED]ms)` line is still logged)

**Checkpoint**: US2 complete — 2xx logs at INFO, 4xx at WARN, 5xx at ERROR.
The filter correctly wraps Spring Security (401 unauthenticated requests appear as WARN).

---

## Phase 5: User Story 3 — Removal Verification (Priority: P3)

**Goal**: Confirm zero legacy references remain in the codebase — no old class names,
no AOP service-logging aspects, and no persistence mechanisms.

**Independent Test**: All grep checks below return empty output AND `mvn clean install`
succeeds.

### Implementation for User Story 3

- [ ] T011 [P] [US3] Verify zero legacy class references: run `grep -r "AppLogger\|HttpRequestResponseInterceptorUtils\|LoggingUtils" src/main/java/` — output MUST be empty
- [ ] T012 [P] [US3] Verify no AOP service-logging advices remain: run `grep -rn "@Around\|@AfterThrowing" src/main/java/` — output MUST be empty (these were only in `AppLogger`)
- [ ] T013 [P] [US3] Verify no HTTP exchange persistence: run `grep -r "HttpExchangeRepository\|HttpExchanges" src/` — output MUST be empty
- [ ] T014 [US3] Run full build and startup smoke test: `mvn clean install` completes with zero errors; then `mvn spring-boot:run` starts without `NoSuchBeanDefinitionException` or `ClassNotFoundException`

**Checkpoint**: All three user stories are independently functional and the legacy
codebase is fully removed.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, documentation tidy-up, and operational readiness.

- [ ] T015 [P] Execute `specs/001-spring-http-logging/quickstart.md` validation checklist end-to-end: all seven checklist items MUST pass
- [ ] T016 [P] Update `CLAUDE.md` Recent Changes section to reflect that `AppLogger`, `HttpRequestResponseInterceptorUtils`, and `LoggingUtils` are removed and replaced by `CommonsRequestLoggingFilter` + `HttpResponseLoggingFilter`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify baseline before touching anything
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — can start immediately after legacy deletion
- **US2 (Phase 4)**: Depends on Phase 3 (T006 must exist before T009 modifies the same file)
- **US3 (Phase 5)**: Depends on Phase 3 + Phase 4 completion
- **Polish (Phase 6)**: Depends on all user stories complete

### Within Phase 2 (Deletion)

- T002, T003, T004 are [P] — delete all three files simultaneously
- T005 depends on T002 + T003 + T004

### Within Phase 3 (US1)

- T006 and T007 are [P] — different files, no dependency between them

### Within Phase 4 (US2)

- T008 must precede T009 (T009 adds a bean for the class created in T008)
- T010 modifies the same file as T008 — do sequentially after T008

### Within Phase 5 (US3)

- T011, T012, T013 are [P] — independent grep checks
- T014 depends on T011 + T012 + T013 (confirm all clean before running build)

---

## Parallel Execution Examples

### Phase 2 — Run all deletions together

```bash
# Delete all three legacy files simultaneously
rm src/main/java/com/app/ecommerce/logging/AppLogger.java
rm src/main/java/com/app/ecommerce/logging/HttpRequestResponseInterceptorUtils.java
rm src/main/java/com/app/ecommerce/logging/LoggingUtils.java
mvn compile   # T005 — verify clean after all three gone
```

### Phase 5 — Run all verifications together

```bash
# Run all three grep checks in parallel
grep -r "AppLogger\|HttpRequestResponseInterceptorUtils\|LoggingUtils" src/main/java/
grep -rn "@Around\|@AfterThrowing" src/main/java/
grep -r "HttpExchangeRepository\|HttpExchanges" src/
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1: Baseline compile check
2. Phase 2: Delete legacy classes (blocking prerequisite)
3. Phase 3: Add `CommonsRequestLoggingFilter` bean + log level property
4. **STOP and VALIDATE**: start app, send a request, confirm DEBUG log appears
5. Commit: `feat(logging): replace AppLogger with CommonsRequestLoggingFilter`

### Full Delivery

1. MVP (above) → commit
2. Phase 4: Add `HttpResponseLoggingFilter` with status-aware levels → commit
3. Phase 5: Run removal verification checks → confirm all green
4. Phase 6: Quickstart checklist + CLAUDE.md update → commit
5. PR against `main` with reference to `specs/001-spring-http-logging/`

---

## Notes

- [P] tasks operate on different files — safe to parallelize
- T009 MUST come after T008 (same config file; T009 adds a bean for T008's class)
- T005 and T014 are mandatory compile/build checkpoints — do not skip them
- `responseWrapper.copyBodyToResponse()` in T008 is critical: omitting it causes the response body to never reach the client
- No new Maven dependencies are introduced (all classes are in `spring-boot-starter-web`)
