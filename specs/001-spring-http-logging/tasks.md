---
description: "Task list for Spring Built-in HTTP Request/Response Logging"
---

# Tasks: Spring Built-in HTTP Request/Response Logging

**Input**: Design documents from `/specs/001-spring-http-logging/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, contracts/logging-contract.md ✅

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story this task belongs to (US1, US2)

## Path Conventions

All source paths are under `src/main/java/com/app/ecommerce/` (single Maven module).

---

## Phase 1: Setup

**Purpose**: No new project structure is needed — all packages already exist.
This phase confirms the working environment before any changes are made.

- [x] T001 Confirm project compiles on current branch: run `mvn compile` from repo root and verify zero errors before any changes

---

## Phase 2: Foundational — Delete Legacy Logging Classes

**Purpose**: Remove the three legacy classes that will be replaced. These MUST be
deleted before adding new beans to avoid duplicate filter registrations and stale
AOP advice.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T002 [P] Delete `src/main/java/com/app/ecommerce/logging/AppLogger.java` — removes the OncePerRequestFilter + @Aspect bean (includes @Around and @AfterThrowing advices; do NOT migrate them anywhere)
- [x] T003 [P] Delete `src/main/java/com/app/ecommerce/logging/HttpRequestResponseInterceptorUtils.java`
- [x] T004 [P] Delete `src/main/java/com/app/ecommerce/logging/LoggingUtils.java`
- [x] T005 Verify project still compiles after all three deletions: run `mvn compile` and confirm zero errors (no dangling autowired references should remain since these classes were only used by each other)

**Checkpoint**: Foundation ready — three legacy files gone, project compiles cleanly.

---

## Phase 3: User Story 1 — Full Request Logging via Bean (Priority: P1) 🎯 MVP

**Goal**: Incoming HTTP requests are logged using Spring's built-in
`CommonsRequestLoggingFilter`, configured as a `@Bean` with no custom prefix strings.

**Independent Test**: Start the app, send `POST /ecommerce/api/v1/auth/login`, and
confirm the application log contains a `DEBUG` line from
`org.springframework.web.filter.CommonsRequestLoggingFilter` showing the URI and payload.

### Implementation for User Story 1

- [x] T006 [US1] Create `src/main/java/com/app/ecommerce/config/HttpLoggingConfiguration.java` as a `@Configuration` class containing a `@Bean` method named `requestLoggingFilter()` that returns a `CommonsRequestLoggingFilter` with: `setIncludeQueryString(true)`, `setIncludePayload(true)`, `setMaxPayloadLength(10_000)`, `setIncludeHeaders(false)` — no custom prefix strings
- [x] T007 [US1] Add the following property to `src/main/resources/application.properties`: `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG`

**Checkpoint**: US1 complete — start the application, send any request, and observe
Spring's default `After request [...]` DEBUG line in the log.

---

## Phase 4: User Story 2 — Removal Verification (Priority: P2)

**Goal**: Confirm zero legacy references remain in the codebase — no old class names,
no AOP service-logging aspects, and no persistence mechanisms.

**Independent Test**: All grep checks below return empty output AND `mvn clean install`
succeeds.

### Implementation for User Story 2

- [x] T008 [P] [US2] Verify zero legacy class references: run `grep -r "AppLogger\|HttpRequestResponseInterceptorUtils\|LoggingUtils" src/main/java/` — output MUST be empty
- [x] T009 [P] [US2] Verify no AOP service-logging advices remain: run `grep -rn "@Around\|@AfterThrowing" src/main/java/` — output MUST be empty (these were only in `AppLogger`)
- [x] T010 [P] [US2] Verify no HTTP exchange persistence: run `grep -r "HttpExchangeRepository\|HttpExchanges" src/` — output MUST be empty
- [x] T011 [US2] Run full build and startup smoke test: `mvn clean install` completes with zero errors; then `mvn spring-boot:run` starts without `NoSuchBeanDefinitionException` or `ClassNotFoundException`

**Checkpoint**: Both user stories are independently functional and the legacy
codebase is fully removed.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation tidy-up.

- [x] T012 [P] Execute `specs/001-spring-http-logging/quickstart.md` validation checklist end-to-end: all checklist items MUST pass
- [x] T013 [P] Update `CLAUDE.md` Recent Changes section to reflect that `AppLogger`, `HttpRequestResponseInterceptorUtils`, and `LoggingUtils` are removed and replaced by `CommonsRequestLoggingFilter` bean only

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify baseline before touching anything
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — can start immediately after legacy deletion
- **US2 (Phase 4)**: Depends on Phase 3 completion
- **Polish (Phase 5)**: Depends on both user stories complete

### Within Phase 2 (Deletion)

- T002, T003, T004 are [P] — delete all three files simultaneously
- T005 depends on T002 + T003 + T004

### Within Phase 3 (US1)

- T006 and T007 are [P] — different files, no dependency between them

### Within Phase 4 (US2)

- T008, T009, T010 are [P] — independent grep checks
- T011 depends on T008 + T009 + T010 (confirm all clean before running build)

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

### Phase 4 — Run all verifications together

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
2. Phase 4: Run removal verification checks → confirm all green
3. Phase 5: Quickstart checklist + CLAUDE.md update → commit
4. PR against `main` with reference to `specs/001-spring-http-logging/`

---

## Notes

- [P] tasks operate on different files — safe to parallelize
- T005 and T011 are mandatory compile/build checkpoints — do not skip them
- No new Maven dependencies are introduced (all classes are in `spring-boot-starter-web`)
- `HttpResponseLoggingFilter` MUST NOT be created — response status logging is out of scope
