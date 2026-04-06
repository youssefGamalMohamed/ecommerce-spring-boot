# Implementation Plan: Spring Boot 2026 Dependency Upgrade

**Branch**: `007-spring-boot-upgrade` | **Date**: 2026-04-06 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/007-spring-boot-upgrade/spec.md`

---

## Summary

Upgrade all Maven dependencies in `pom.xml` from their current versions (Spring Boot 3.0.0, era 2022)
to the latest stable releases as of Q1 2026 (Spring Boot 4.0.5), then validate the application builds,
starts, and passes all runtime checks. The upgrade is executed in two phases:

- **Phase 1** — Advance to Spring Boot 3.5.9 (latest 3.x), update all third-party dependencies, and
  migrate `JwtService.java` to the JJWT 0.12.x API (whose old API was removed, not just deprecated).
- **Phase 2** — Advance from Spring Boot 3.5.9 to Spring Boot 4.0.5 (latest GA), update SpringDoc to 3.0.2
  (the compatible 3.x release line), and replace the deprecated Jackson 2 declaration with Jackson 3 coordinates.

No new domain packages, entities, or API endpoints are introduced. The only source code changes are
`JwtService.java` (JJWT API migration) and `application.yml` (deprecated Hibernate dialect removal).
All other changes are version number updates in `pom.xml`.

---

## Technical Context

**Language/Version**: Java 17 (unchanged — Spring Boot 4.0 minimum is Java 17)
**Primary Dependencies**: Spring Boot 4.0.5, Spring Framework 7.x (managed), Spring Security 7.x (managed)
**Storage**: MySQL 8 (server), MySQL Connector/J 9.6.0 (driver), Redis (Lettuce, Spring Boot managed)
**Testing**: spring-boot-starter-test (JUnit 5, managed)
**Target Platform**: Linux server (unchanged)
**Project Type**: REST API web service (single-module Maven mono-repo)
**Performance Goals**: No regression from pre-upgrade baseline (startup within ±20%)
**Constraints**: Java 17 must remain the baseline — no JVM upgrade in scope
**Scale/Scope**: Single bounded context; all changes are dependency-level, not architectural

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Domain-Based Layered Architecture | ✅ PASS | No structural changes; domain packages untouched |
| II. DTO-First Communication | ✅ PASS | No new DTOs; existing MapStruct mappers unchanged |
| III. JWT Stateless Authentication | ✅ PASS | `SessionCreationPolicy.STATELESS` unchanged; `SecurityConfig` unchanged |
| IV. Interface-Driven Design | ✅ PASS | No new controllers or services |
| V. Monetary Precision | ✅ PASS | No monetary field changes |
| VI. Transactional Integrity | ✅ PASS | No `@Transactional` changes |
| VII. Observability | ✅ PASS | `CommonsRequestLoggingFilter` and Actuator config unchanged |
| VIII. API Documentation & Swagger Security | ✅ PASS | `@SecurityRequirements` pattern unchanged; SpringDoc 3.x supports it |

**Constitution amendment required**: Yes (PATCH) — Technology Stack table in `constitution.md` must be
updated to reflect the new Spring Boot, JJWT, MySQL Connector/J, MapStruct, Lombok, and SpringDoc versions
after the upgrade is validated. This is a PATCH amendment (no principles changed, only version references updated).

---

## Project Structure

### Documentation (this feature)

```text
specs/007-spring-boot-upgrade/
├── plan.md              ← this file
├── research.md          ← Phase 0 output (complete)
├── quickstart.md        ← Phase 1 output (step-by-step developer guide)
├── checklists/
│   └── requirements.md  ← Spec quality checklist
└── tasks.md             ← Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

No new directories or packages are created. All changes are within existing files:

```text
pom.xml                                          ← Version bumps (all phases)
src/main/resources/application.yml              ← Hibernate dialect removal (Phase 1)
src/main/java/com/app/ecommerce/
└── shared/
    └── security/
        └── JwtService.java                      ← JJWT 0.12.x API migration (Phase 1)
```

**Structure Decision**: Single-module Maven mono-repo with domain-first packaging. No structural changes.

---

## Implementation Phases

### Phase 1 — Upgrade to Spring Boot 3.5.9 (Milestone A)

> Goal: Reach a green build and green context-load test on Spring Boot 3.5.9 before attempting 4.0.

#### Step 1.1 — Update `pom.xml` version properties and parent

Update the Spring Boot parent and all explicit version strings/properties:

| Location in pom.xml | Old Value | New Value |
|---|---|---|
| `<parent><version>` | `3.0.0` | `3.5.9` |
| `${org.mapstruct.version}` | `1.6.0` | `1.6.3` |
| `${maven.version}` (compiler plugin) | `3.13.0` | `3.15.0` |
| `${lombok.version}` | `1.18.42` | `1.18.44` |
| `mysql-connector-j` explicit version | `8.0.31` | `9.6.0` |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` version | `0.11.5` | `0.12.6` |
| `springdoc-openapi-starter-webmvc-ui` version | `2.0.2` | `2.8.16` |

Add the properties-migrator dependency (remove it after Phase 2 is complete):

```xml
<!-- TEMPORARY: Detects Spring Boot property renames at startup; remove after upgrade -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-properties-migrator</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### Step 1.2 — Migrate `JwtService.java` to JJWT 0.12.x API

The JJWT 0.11.x API has been **removed** (not deprecated) in 0.12.x — the project will not compile
without these changes.

**In `JwtService.java`:**

1. Change `getSignInKey()` return type from `Key` to `SecretKey`
   - `import java.security.Key` → `import javax.crypto.SecretKey`
   - Return type: `private Key getSignInKey()` → `private SecretKey getSignInKey()`
   - Body unchanged: `Keys.hmacShaKeyFor(keyBytes)` already returns `SecretKey`

2. Remove `import io.jsonwebtoken.SignatureAlgorithm`

3. Update token builder in `generateToken()`:
   - `.setClaims(extraClaims)` → `.claims(extraClaims)`
   - `.setSubject(userDetails.getUsername())` → `.subject(userDetails.getUsername())`
   - `.setIssuedAt(new Date(...))` → `.issuedAt(new Date(...))`
   - `.setExpiration(new Date(...))` → `.expiration(new Date(...))`
   - `.signWith(getSignInKey(), SignatureAlgorithm.HS256)` → `.signWith(getSignInKey())`
     (algorithm is inferred automatically from the SecretKey type)

4. Update parser in `extractAllClaims()`:
   - `Jwts.parserBuilder()` → `Jwts.parser()`
   - `.setSigningKey(getSignInKey())` → `.verifyWith(getSignInKey())`
   - `.parseClaimsJws(token)` → `.parseSignedClaims(token)`
   - `.getBody()` → `.getPayload()`

#### Step 1.3 — Fix deprecated Hibernate dialect in `application.yml`

Remove the `spring.jpa.database-platform` property entirely. Spring Boot auto-detects the correct
MySQL dialect from the connector on the classpath.

```yaml
# REMOVE this line from application.yml:
#   database-platform: org.hibernate.dialect.MySQL8Dialect
```

The `spring.jpa` block becomes:

```yaml
spring:
  jpa:
    generate-ddl: true
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
```

#### Step 1.4 — Build and validate Phase 1

Run the following checks in order. Each must pass before proceeding to the next:

1. `mvn clean compile` — must produce zero compilation errors
2. Check startup logs for properties-migrator output — address any reported property renames
3. `mvn spring-boot:run` — application must reach STARTED state, `GET /actuator/health` must return UP
4. `mvn test` — `contextLoads` test must pass
5. Manual smoke test: POST `/auth/login`, verify JWT returned; GET `/products`, verify unauthenticated access works; GET `/orders` with no token, verify 401 returned

---

### Phase 2 — Upgrade to Spring Boot 4.0.5 (Final Target)

> Prerequisite: Phase 1 fully validated (green build, green test, green smoke test).

#### Step 2.1 — Update `pom.xml` for Spring Boot 4.0.5

| Location in pom.xml | Old Value | New Value |
|---|---|---|
| `<parent><version>` | `3.5.9` | `4.0.5` |
| `springdoc-openapi-starter-webmvc-ui` version | `2.8.16` | `3.0.2` |
| `jackson-datatype-jsr310` groupId | `com.fasterxml.jackson.datatype` | `tools.jackson.datatype` |

The `jackson-datatype-jsr310` dependency currently has no explicit version (managed by Spring Boot BOM).
Only the `groupId` changes:

```xml
<!-- Before (SB 3.x) -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- After (SB 4.0) -->
<dependency>
    <groupId>tools.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

#### Step 2.2 — Remove the properties-migrator

Remove the temporary `spring-boot-properties-migrator` dependency added in Step 1.1.

#### Step 2.3 — Build and validate Phase 2

Run the following checks in order:

1. `mvn clean compile` — must produce zero compilation errors (Jackson 3 group ID change is the most
   likely compile-time failure if the BOM resolution differs)
2. `mvn spring-boot:run` — application must reach STARTED state; verify in startup logs that there are
   no `NoClassDefFoundError` or `ClassNotFoundException` for Jackson classes
3. `GET /actuator/health` — must return `{"status":"UP"}` with database and redis both up
4. `mvn test` — `contextLoads` test must pass
5. Manual smoke test (repeat Phase 1 checks):
   - `POST /auth/login` → JWT returned with same JSON structure
   - `GET /products` → unauthenticated, returns product list
   - `GET /categories` → unauthenticated, returns category list
   - `GET /orders` without token → 401
   - `GET /orders` with admin token → 200
   - `GET /actuator/metrics` with no token → 401 (protected by ADMIN role)
   - Swagger UI at `/ecommerce/api/v1/swagger-ui` loads; login flow works (Principle VIII)

---

### Phase 3 — Post-Upgrade Cleanup and Documentation

#### Step 3.1 — Constitution amendment

Update `.specify/memory/constitution.md` Technology Stack table (PATCH amendment):

| Field | Old | New |
|---|---|---|
| Framework | Spring Boot 3.0.0 | Spring Boot 4.0.5 |
| Security | Spring Security + JJWT 0.11.5 | Spring Security + JJWT 0.12.6 |
| Database (driver) | MySQL Connector/J 8.0.31 | MySQL Connector/J 9.6.0 |
| DTO Mapping | MapStruct 1.6.0 | MapStruct 1.6.3 |
| API Documentation | SpringDoc OpenAPI 2.0.2 | SpringDoc OpenAPI 3.0.2 |
| Boilerplate | Lombok 1.18.42 | Lombok 1.18.44 |

Also update `CLAUDE.md` Active Technologies table with the same changes.

#### Step 3.2 — Clean up build output

- Verify `mvn clean install -DskipTests` succeeds
- Verify the packaged JAR starts with `java -jar target/*.jar`

---

## Complexity Tracking

No constitution violations introduced. No new packages, patterns, or cross-cutting concerns added.

---

## Common Implementation Issues

### Issue 1: JJWT `SecretKey` vs `Key` type mismatch
**Symptom**: Compile error `incompatible types: Key cannot be converted to SecretKey`
**Fix**: Change `getSignInKey()` return type to `SecretKey` and update import to `javax.crypto.SecretKey`.
`Keys.hmacShaKeyFor()` already returns `SecretKey` — only the declared return type needs updating.

### Issue 2: JJWT `Jws<Claims>` accessor
**Symptom**: Compile error on `parseSignedClaims()` result
**Fix**: Call `.getPayload()` instead of `.getBody()` on the returned `Jws<Claims>` object.

### Issue 3: Hibernate `NoSuchMethodError` at startup
**Symptom**: Application fails to start with Hibernate class not found
**Fix**: Ensure `spring.jpa.database-platform` is fully removed from `application.yml`.
Spring Boot auto-detection is reliable for MySQL 8/9.

### Issue 4: SpringDoc UI returns 404 after SpringDoc 3.0 upgrade
**Symptom**: `/swagger-ui` returns 404
**Fix**: `springdoc.swagger-ui.path=/swagger-ui` in `application.yml` should still work. If not,
verify Spring Boot context path is included in the URL: `/ecommerce/api/v1/swagger-ui`.

### Issue 5: Jackson class not found at runtime after SB 4.0 upgrade
**Symptom**: `NoClassDefFoundError: com/fasterxml/jackson/...`
**Fix**: The `jackson-datatype-jsr310` groupId must be changed to `tools.jackson.datatype`.
Also check if any other Jackson 2 artifacts are declared with the old `com.fasterxml.jackson` groupId.
