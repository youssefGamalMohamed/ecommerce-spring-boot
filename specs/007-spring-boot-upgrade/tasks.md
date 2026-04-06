---
description: "Task list for Spring Boot 2026 Dependency Upgrade"
---

# Tasks: Spring Boot 2026 Dependency Upgrade

**Input**: Design documents from `/specs/007-spring-boot-upgrade/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, quickstart.md ✅

**Tests**: Not included — no new tests requested in spec.md; `EcommerceApplicationTests.contextLoads` is the only existing test and requires no changes.

**Organization**: Tasks follow the two-phase upgrade strategy from plan.md. US1 (dependency version changes) must complete in full before US2/US3/US4 validation phases. All validation phases are sequential by design — each depends on the previous phase passing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no ordering dependency)
- **[Story]**: Which user story this task belongs to (US1–US4)
- Exact file paths included for every task

---

## Phase 1: Setup (Pre-Upgrade Baseline Verification)

**Purpose**: Verify the repository is in a clean, known-good state before any changes are made. Establishes the baseline that the upgrade must preserve.

- [X] T001 Confirm working branch: run `git branch` and verify output shows `* 007-spring-boot-upgrade`; if not, switch to it with `git checkout 007-spring-boot-upgrade`
- [X] T002 Capture baseline dependency tree for later comparison: run `mvn dependency:tree -Doutput=/tmp/before-upgrade-deps.txt` from repository root
- [X] T003 Confirm zero compilation errors on the current (pre-upgrade) codebase: run `mvn clean compile`; must exit with `BUILD SUCCESS` — if it fails, fix the issue before proceeding
- [X] T004 Confirm all existing tests pass on the current (pre-upgrade) codebase: run `mvn test`; `EcommerceApplicationTests.contextLoads` must pass — if it fails, fix before proceeding

**Checkpoint**: Baseline confirmed — zero errors, all tests green. Safe to start upgrading.

---

## Phase 2: Foundational

No foundational infrastructure work required — this is a single-module Maven project with no structural changes. All changes are dependency-level modifications within existing files.

**The three files that change**:
- `pom.xml` — version bumps + Jackson group ID change
- `src/main/resources/application.yml` — remove deprecated Hibernate dialect property
- `src/main/java/com/app/ecommerce/shared/security/JwtService.java` — JJWT 0.12.x API migration

---

## Phase 3: User Story 1 — Upgrade All Dependencies to Latest Stable Versions (Priority: P1) 🎯 MVP

**Goal**: Update all declared dependency versions in `pom.xml`, remove the deprecated Hibernate dialect in `application.yml`, and migrate `JwtService.java` to the JJWT 0.12.x API (whose 0.11.x API was removed — not deprecated — in 0.12.x). Done in two sub-phases: first reach Spring Boot 3.5.9, validate it compiles, then advance to 4.0.5.

**Independent Test**: Run `mvn clean package -DskipTests` — must produce a `.jar` in `target/` with zero build errors.

### Sub-Phase A: Upgrade to Spring Boot 3.5.9 (Intermediate Milestone)

> All tasks in this sub-phase are changes to `pom.xml`, `application.yml`, and `JwtService.java`.
> Complete all of them before running the compile check in T020.

#### pom.xml — Spring Boot parent and version properties

- [X] T005 [US1] Update Spring Boot parent `<version>` from `3.0.0` to `3.5.9` in `pom.xml` (inside `<parent>` block, currently line 8); after this change the parent block reads `<version>3.5.9</version>`
- [X] T006 [P] [US1] Update `${org.mapstruct.version}` property value from `1.6.0` to `1.6.3` in `pom.xml` `<properties>` block (currently line 20); also update the `<version>${org.mapstruct.version}</version>` reference inside `<annotationProcessorPaths>` — this reference is already using the property so no separate change is needed there
- [X] T007 [P] [US1] Update `${maven.version}` property value from `3.13.0` to `3.15.0` in `pom.xml` `<properties>` block (currently line 21); the `maven-compiler-plugin` `<version>` already uses `${maven.version}` so it inherits the update automatically
- [X] T008 [P] [US1] Update `${lombok.version}` property value from `1.18.42` to `1.18.44` in `pom.xml` `<properties>` block (currently line 23); both the `<dependency>` for `lombok` and the `<annotationProcessorPaths>` entry for `lombok` use `${lombok.version}` so they inherit automatically

#### pom.xml — Explicit dependency version bumps

- [X] T009 [P] [US1] Update `mysql-connector-j` explicit `<version>` from `8.0.31` to `9.6.0` in `pom.xml` (inside the `<!-- DATABASE -->` section, currently line 75); keep `<scope>runtime</scope>` unchanged; JDBC URL `jdbc:mysql://localhost:3306/ecommerce` and driver class `com.mysql.cj.jdbc.Driver` remain valid in 9.x
- [X] T010 [P] [US1] Update `jjwt-api` `<version>` from `0.11.5` to `0.12.6` in `pom.xml` (inside `<!-- SECURITY -->` section, currently line 108); this is the compile-time API jar — keep no `<scope>` (defaults to compile)
- [X] T011 [P] [US1] Update `jjwt-impl` `<version>` from `0.11.5` to `0.12.6` in `pom.xml` (currently line 113); keep `<scope>runtime</scope>` unchanged — this is the runtime implementation, never on the compile classpath
- [X] T012 [P] [US1] Update `jjwt-jackson` `<version>` from `0.11.5` to `0.12.6` in `pom.xml` (currently line 118); keep `<scope>runtime</scope>` unchanged — this provides JSON serialization support for JJWT at runtime
- [X] T013 [P] [US1] Update `springdoc-openapi-starter-webmvc-ui` `<version>` from `2.0.2` to `2.8.16` in `pom.xml` (inside `<!-- API DOCUMENTATION -->` section, currently line 129); note: 2.8.16 is for Spring Boot 3.x; the final 4.x version (3.0.2) is applied in Sub-Phase B

#### pom.xml — Add temporary properties migrator (Spring Boot property rename detector)

- [X] T014 [US1] Add the `spring-boot-properties-migrator` dependency to `pom.xml` inside `<dependencies>`, placed immediately before the closing `</dependencies>` tag and after the `spring-boot-starter-test` block; add with `<scope>runtime</scope>` only — no `<version>` (BOM-managed); full block to add:
  ```xml
  <!-- TEMPORARY: Detects Spring Boot property renames at startup; remove in T033 -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-properties-migrator</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```

#### application.yml — Remove deprecated Hibernate dialect

- [X] T015 [US1] Remove the line `database-platform: org.hibernate.dialect.MySQL8Dialect` from `src/main/resources/application.yml` (currently line 16, inside `spring.jpa` block); after removal the `spring.jpa` block must read exactly:
  ```yaml
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
  **Reason**: `MySQL8Dialect` was deprecated in Hibernate 6.2 and removed in Hibernate 6.4 (managed by Spring Boot 3.2+). Removing the property lets Spring Boot auto-detect the correct dialect from the MySQL connector jar on the classpath.

#### JwtService.java — JJWT 0.12.x API migration (compile-breaking changes)

> **CRITICAL**: The JJWT 0.11.x API was entirely **removed** (not merely deprecated) in 0.12.x.
> All usages of the old API in `JwtService.java` will cause **compilation errors** until these tasks are complete.
> Apply all T016–T025 before running the compile check in T026.

- [X] T016 [US1] Remove the import line `import io.jsonwebtoken.SignatureAlgorithm;` from `src/main/java/com/app/ecommerce/shared/security/JwtService.java` (currently line 5); this class no longer exists in JJWT 0.12.x — the algorithm is now inferred from the `SecretKey` type automatically
- [X] T017 [US1] Replace `import java.security.Key;` with `import javax.crypto.SecretKey;` in `src/main/java/com/app/ecommerce/shared/security/JwtService.java` (currently line 12); `Keys.hmacShaKeyFor()` already returns a `SecretKey` — only the declared import and return type need updating
- [X] T018 [US1] Change the return type declaration of `getSignInKey()` method from `private Key getSignInKey()` to `private SecretKey getSignInKey()` in `src/main/java/com/app/ecommerce/shared/security/JwtService.java` (currently line 80); the method body `Keys.hmacShaKeyFor(keyBytes)` is unchanged — it already returns `SecretKey`
- [X] T019 [US1] In the `generateToken()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.setClaims(extraClaims)` with `.claims(extraClaims)` (currently line 50); this is the new fluent builder API in JJWT 0.12.x
- [X] T020 [US1] In the `generateToken()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.setSubject(userDetails.getUsername())` with `.subject(userDetails.getUsername())` (currently line 51)
- [X] T021 [US1] In the `generateToken()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.setIssuedAt(new Date(System.currentTimeMillis()))` with `.issuedAt(new Date(System.currentTimeMillis()))` (currently line 52)
- [X] T022 [US1] In the `generateToken()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.setExpiration(new Date(System.currentTimeMillis() + expiration))` with `.expiration(new Date(System.currentTimeMillis() + expiration))` (currently line 53)
- [X] T023 [US1] In the `generateToken()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.signWith(getSignInKey(), SignatureAlgorithm.HS256)` with `.signWith(getSignInKey())` (currently line 54); in JJWT 0.12.x the algorithm is inferred from the `SecretKey` type — `SignatureAlgorithm.HS256` is no longer passed separately
- [X] T024 [US1] In the `extractAllClaims()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `Jwts.parserBuilder()` with `Jwts.parser()` (currently line 73); `parserBuilder()` was removed in 0.12.x
- [X] T025 [US1] In the `extractAllClaims()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.setSigningKey(getSignInKey())` with `.verifyWith(getSignInKey())` (currently line 74); the method name changed from `setSigningKey` to `verifyWith` in 0.12.x and now takes `SecretKey` directly (no cast needed since T018 updated the return type)
- [X] T026 [US1] In the `extractAllClaims()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.parseClaimsJws(token)` with `.parseSignedClaims(token)` (currently line 76); `parseClaimsJws` was removed in 0.12.x
- [X] T027 [US1] In the `extractAllClaims()` method body in `src/main/java/com/app/ecommerce/shared/security/JwtService.java`, replace `.getBody()` with `.getPayload()` (currently line 77); the accessor method was renamed in 0.12.x

After T016–T027 the full `generateToken()` and `extractAllClaims()` methods must look exactly like:
```java
public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts
            .builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey())
            .compact();
}

private Claims extractAllClaims(String token) {
    return Jwts
            .parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
}

private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

#### Sub-Phase A Compile Checkpoint

- [X] T028 [US1] Run `mvn clean compile` — must exit with `BUILD SUCCESS` and zero compilation errors; if it fails, diagnose using the error messages:
  - `cannot find symbol: method setClaims` → T019 not applied
  - `cannot find symbol: class SignatureAlgorithm` → T016 not applied
  - `incompatible types: Key cannot be converted to SecretKey` → T018 not applied
  - `cannot find symbol: method parserBuilder` → T024 not applied
  - `cannot find symbol: method parseClaimsJws` → T026 not applied
  - `cannot find symbol: method getBody` → T027 not applied

- [X] T029 [US1] Run `mvn spring-boot:run` (with MySQL and Redis running locally) and scan the startup log for `[PropertiesMigrationListener]` warning lines; if any appear, note the old→new property rename and update `src/main/resources/application.yml` accordingly before proceeding; expected result is no migration warnings (research.md §3.7 confirms no affected properties in this project)

- [X] T030 [US1] Stop the running application (Ctrl+C) after confirming the startup log shows `Started EcommerceApplication in X.XXX seconds` with no `ERROR` lines

- [X] T031 [US1] Commit Sub-Phase A changes: `git add pom.xml src/main/java/com/app/ecommerce/shared/security/JwtService.java src/main/resources/application.yml && git commit -m "chore(deps): upgrade to Spring Boot 3.5.9 + JJWT 0.12.6 + MySQL 9.6.0"`

---

### Sub-Phase B: Upgrade to Spring Boot 4.0.5 (Final Target)

> Prerequisite: T031 must be committed (Sub-Phase A fully validated and committed).

#### pom.xml — Spring Boot 4.0.5 and related dependency updates

- [X] T032 [US1] Update Spring Boot parent `<version>` from `3.5.9` to `4.0.5` in `pom.xml` (inside `<parent>` block); after this change Spring Framework 7.x and Spring Security 7.x are automatically managed by the BOM — no explicit versions needed for those
- [X] T033 [US1] Update `springdoc-openapi-starter-webmvc-ui` `<version>` from `2.8.16` to `3.0.2` in `pom.xml`; SpringDoc 3.x is the release line dedicated to Spring Boot 4.x — same artifact ID, same `io.swagger.v3.oas.models.*` import package in application code (no Java source changes needed)
- [X] T034 [US1] Change the `jackson-datatype-jsr310` dependency `<groupId>` from `com.fasterxml.jackson.datatype` to `tools.jackson.datatype` in `pom.xml` (currently around line 98–100); do **not** add a `<version>` tag — version remains BOM-managed by Spring Boot 4.0; the `<artifactId>jackson-datatype-jsr310` is unchanged; **reason**: Spring Boot 4.0 ships with Jackson 3 under the `tools.jackson.*` group ID; leaving the old `com.fasterxml.jackson.datatype` group causes `NoClassDefFoundError` at runtime
- [X] T035 [US1] Remove the `spring-boot-properties-migrator` dependency block that was added in T014 from `pom.xml`; this temporary dependency is no longer needed after the 3.x migration is verified

#### Sub-Phase B Compile Checkpoint

- [X] T036 [US1] Run `mvn clean compile` — must exit with `BUILD SUCCESS` and zero errors; the most likely failure here is the Jackson group ID change from T034; if you see `NoClassDefFoundError` or `ClassNotFoundException` with `com/fasterxml/jackson` in the error, verify T034 was applied correctly (check that the groupId in pom.xml now reads `tools.jackson.datatype`)

- [X] T037 [US1] Run `mvn clean package -DskipTests` — must produce `target/EcommerceApp-0.0.1-SNAPSHOT.jar` successfully; this satisfies the US1 independent test criterion: the upgraded build delivers a runnable artifact

**Checkpoint**: US1 complete — all 10 dependency version updates applied, two source file migrations complete, build artifact produced.

---

## Phase 4: User Story 2 — Application Starts and Passes Health Checks After Upgrade (Priority: P2)

**Goal**: Confirm that the Spring Boot 4.0.5 application starts without errors, all infrastructure connections (MySQL 9.6, Redis) initialize correctly, and the health endpoint reports UP.

**Independent Test**: Run `mvn spring-boot:run` → `curl http://localhost:8081/ecommerce/api/v1/actuator/health` → JSON body contains `"status":"UP"`.

**Prerequisite**: MySQL 8 must be running on `localhost:3306` (database `ecommerce` exists), Redis must be running on `localhost:6379`.

- [X] T038 [US2] Start the application: run `mvn spring-boot:run` and wait for `Started EcommerceApplication in X.XXX seconds` in the log; do not proceed if startup fails
- [X] T039 [US2] Scan the startup log for `NoClassDefFoundError` containing `com/fasterxml/jackson` — if found, the Jackson 3 group ID change in T034 did not take effect (check pom.xml); expected: no such error
- [X] T040 [US2] Scan the startup log for `Unsupported dialect class` or `NoSuchMethodError` containing `org.hibernate.dialect.MySQL8Dialect` — if found, T015 (dialect removal from application.yml) was not applied; expected: no dialect error
- [X] T041 [US2] Scan the startup log for any `BeanCreationException` or `UnsatisfiedDependencyException` lines — these indicate broken bean wiring; expected: none; if found, identify the failing bean class from the stack trace
- [X] T042 [US2] In a separate terminal, run `curl http://localhost:8081/ecommerce/api/v1/actuator/health` — response body must contain `"status":"UP"` at the top level; if response is `{"status":"DOWN"}` proceed to T043 to identify the failing component
- [X] T043 [US2] Verify database component health: inspect the health response JSON for `"db":{"status":"UP"}` — confirms MySQL Connector/J 9.6.0 connects to `localhost:3306` correctly; if `"db":{"status":"DOWN"}`, check that MySQL is running and the `ecommerce` database exists
- [X] T044 [US2] Verify Redis component health: inspect the health response JSON for `"redis":{"status":"UP"}` — confirms Lettuce client (managed by Spring Boot) connects to `localhost:6379`; if `"redis":{"status":"DOWN"}`, check that Redis is running
- [X] T045 [US2] Run `mvn test` (application does not need to be running separately — `@SpringBootTest` starts its own context): `EcommerceApplicationTests.contextLoads` must pass; this verifies that all bean wiring, JPA config, and security config are valid under Spring Boot 4.0.5

**Checkpoint**: US2 complete — application starts, MySQL and Redis connect, context loads.

---

## Phase 5: User Story 3 — Security Configuration Remains Intact (Priority: P2)

**Goal**: Verify that after the JJWT 0.12.x migration and Spring Security 7.x (managed by SB 4.0), the authentication and authorization behavior is identical to pre-upgrade: public paths serve unauthenticated requests, protected paths reject them, JWT generation and validation work correctly.

**Independent Test**: Run the four acceptance scenarios from spec.md against the running application — all must return the expected HTTP status codes.

**Prerequisite**: Application must be running (`mvn spring-boot:run` from T038). MySQL and Redis must be UP (verified in Phase 4).

- [X] T046 [US3] Test public product endpoint without authentication: run `curl -i http://localhost:8081/ecommerce/api/v1/products` — expect `HTTP/1.1 200` in the response headers and a JSON array body; this path is whitelisted in `SecurityConfig.java` (`GET /products/**`)
- [X] T047 [US3] Test public category endpoint without authentication: run `curl -i http://localhost:8081/ecommerce/api/v1/categories` — expect `HTTP/1.1 200` and a JSON array body; this path is whitelisted in `SecurityConfig.java` (`GET /categories/**`)
- [X] T048 [US3] Test protected endpoint without authentication: run `curl -i http://localhost:8081/ecommerce/api/v1/orders` — expect `HTTP/1.1 401` in the response headers; a 403 would indicate the request reached authorization (wrong), a 500 would indicate a filter error (wrong), and a 200 would indicate the path was accidentally whitelisted (wrong)
- [X] T049 [US3] Obtain a JWT via the login endpoint: run `curl -X POST http://localhost:8081/ecommerce/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@test.com","password":"password"}'`; expect `HTTP/1.1 200` and a JSON response body; extract the `accessToken` value from the response for use in T050–T053
- [X] T050 [US3] Validate JWT structure: decode the base64url-encoded middle segment (payload) of the returned JWT by running `echo "<PAYLOAD_SEGMENT>" | base64 -d`; verify the decoded JSON contains `"sub"` (subject/username), `"iat"` (issued-at timestamp), and `"exp"` (expiration timestamp) — confirms JJWT 0.12.x `generateToken()` produces well-formed JWTs
- [X] T051 [US3] Test protected endpoint with valid admin JWT: run `curl -i -H "Authorization: Bearer <ACCESS_TOKEN>" http://localhost:8081/ecommerce/api/v1/orders` — expect `HTTP/1.1 200` and a JSON response body; this verifies `JwtAuthenticationFilter` correctly validates JJWT 0.12.x tokens
- [X] T052 [US3] Test admin-only actuator endpoint with admin JWT: run `curl -i -H "Authorization: Bearer <ACCESS_TOKEN>" http://localhost:8081/ecommerce/api/v1/actuator/metrics` — expect `HTTP/1.1 200`; this path is protected by `ADMIN` role per `SecurityConfig.java`
- [X] T053 [US3] Test admin-only actuator endpoint without any token: run `curl -i http://localhost:8081/ecommerce/api/v1/actuator/metrics` — expect `HTTP/1.1 401`; confirms the ADMIN-role protection is still enforced
- [X] T054 [US3] Test with an invalid JWT (tampered signature): run `curl -i -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalid_signature" http://localhost:8081/ecommerce/api/v1/orders` — expect `HTTP/1.1 401` (not 500); confirms `JwtService.extractAllClaims()` handles invalid tokens gracefully via the `JwtAuthenticationFilter` exception handling

**Checkpoint**: US3 complete — JJWT 0.12.x generates and validates tokens correctly; all security rules unchanged.

---

## Phase 6: User Story 4 — All Existing API Endpoints Behave Correctly After Upgrade (Priority: P3)

**Goal**: Confirm that all REST API endpoint contracts (request format, response format, HTTP status codes, serialization behavior) are identical to pre-upgrade behavior. Validates Jackson 3 serialization, SpringDoc 3.0.2 Swagger UI, and end-to-end request flows.

**Independent Test**: Run `mvn test` (all tests pass) + execute manual smoke flows covering all domain areas.

**Prerequisite**: Application running (from Phase 5), admin JWT obtained (from T049).

- [X] T055 [US4] Run the full test suite: `mvn test` — all tests must pass; `EcommerceApplicationTests.contextLoads` must be green; if it fails here (it passed in T045), check if a prior smoke test left the application in an inconsistent state
- [X] T056 [US4] Test date/time serialization — call an endpoint that returns a `LocalDate` or `LocalDateTime` field (e.g., an order response) using `curl -H "Authorization: Bearer <TOKEN>" http://localhost:8081/ecommerce/api/v1/orders`; verify date fields are serialized as ISO-8601 strings (e.g., `"createdAt":"2026-04-06T10:00:00"`) not as numeric arrays; this validates `tools.jackson.datatype:jackson-datatype-jsr310` (T034) is active and correctly registered
- [X] T057 [US4] Verify Swagger UI loads: open `http://localhost:8081/ecommerce/api/v1/swagger-ui` in a browser (or run `curl -i http://localhost:8081/ecommerce/api/v1/swagger-ui/index.html`); expect HTTP 200 and HTML content; if 404, check that `springdoc.swagger-ui.path=/swagger-ui` is still present in `src/main/resources/application.yml` (unchanged from baseline)
- [X] T058 [US4] Verify Swagger UI Constitution Principle VIII compliance: in the browser Swagger UI, confirm that `POST /auth/login` appears **without** a padlock icon (public endpoint, no auth required), and that `GET /orders` shows a padlock (protected endpoint); this verifies SpringDoc 3.0.2 correctly reads `@SecurityRequirement` annotations on controller interfaces
- [X] T059 [US4] Test Swagger UI authentication flow: in the browser Swagger UI, click "Authorize", paste the JWT from T049 in the `Bearer` field, then execute `GET /orders` from the UI — expect a successful response; confirms the Swagger authorize flow works with SpringDoc 3.0.2
- [X] T060 [US4] Test response structure consistency — compare a `GET /products` response from the upgraded app to what the endpoint returned before the upgrade (reference the pre-upgrade behavior from research.md); verify the JSON keys, nesting, and field types are identical; specifically check that `BigDecimal` price fields serialize as JSON numbers (not strings)
- [X] T061 [US4] Test customer-role access restriction (if a CUSTOMER-role user account exists in the database): obtain a CUSTOMER JWT via `POST /auth/login` with customer credentials; call an ADMIN-only endpoint (e.g., `POST /products` to create a product) — expect `HTTP/1.1 403` Forbidden (not 401, not 500); this verifies `@PreAuthorize("hasRole('ADMIN')")` still enforces role-based access
- [X] T062 [US4] Test request validation still works: call `POST /auth/login` with an empty body `{}` — expect `HTTP/1.1 400 Bad Request` with an error response body (not 500); this verifies Jakarta Bean Validation (managed by Spring Boot 4.0 BOM) is still active
- [X] T063 [US4] Run full build including packaging: `mvn clean install -DskipTests` — must produce `target/EcommerceApp-0.0.1-SNAPSHOT.jar` with `BUILD SUCCESS`
- [X] T064 [US4] Verify the packaged JAR starts independently (without Maven): run `java -jar target/EcommerceApp-0.0.1-SNAPSHOT.jar` with MySQL and Redis running; application must reach `Started EcommerceApplication in X.XXX seconds`; then run `curl http://localhost:8081/ecommerce/api/v1/actuator/health` — expect `{"status":"UP"}`; stop the application after verification (Ctrl+C)
- [X] T065 [US4] Verify Redis cache behavior is intact (FR-008): with the application running via `mvn spring-boot:run`, call `GET /products` once to populate the cache, then call it a second time; inspect the application log (log level `DEBUG` for `org.hibernate.SQL`) — the second call must NOT emit a `SELECT` SQL statement (cache hit); if SQL appears on both calls, the `tools.jackson.datatype:jackson-datatype-jsr310` Jackson 3 module (T034) may not be registering correctly and Redis is failing to deserialize cached objects, falling back to DB; alternatively verify via `redis-cli KEYS "*products*"` — at least one key must exist after the first call
- [X] T066 [US4] Cart API smoke test: call `curl -i -H "Authorization: Bearer <TOKEN>" http://localhost:8081/ecommerce/api/v1/cart` — accept HTTP 200 (cart exists) or 404 (no cart yet); reject HTTP 500; this exercises `CartServiceImpl` and `CartMapper` which both use Lombok `@Builder` and MapStruct (upgraded in Sub-Phase A to 1.18.44 / 1.6.3); if 500 is returned, check startup log for `MapperException` or `NullPointerException` in the cart package
- [X] T067 [US4] Order detail endpoint smoke test: if orders exist in the database, call `curl -i -H "Authorization: Bearer <TOKEN>" http://localhost:8081/ecommerce/api/v1/orders` to get an order ID, then call `curl -i -H "Authorization: Bearer <TOKEN>" http://localhost:8081/ecommerce/api/v1/orders/{id}` — expect HTTP 200 with a JSON body containing nested `items` and `deliveryInfo` sub-objects; verify the response body does **not** contain `LazyInitializationException` text (a Hibernate 7.1 regression risk when fetching lazy associations)
- [X] T068 [US4] Commit Sub-Phase B (Spring Boot 4.0.5) changes: `git add pom.xml && git commit -m "chore(deps): upgrade to Spring Boot 4.0.5 + SpringDoc 3.0.2 + Jackson 3"`

**Checkpoint**: US4 complete — all API endpoints, serialization, caching, and Swagger UI behave identically to pre-upgrade.

---

## Phase 7: Polish & Post-Upgrade Documentation

**Purpose**: Update project documentation to record the new dependency versions, satisfying the constitution amendment requirement from plan.md (PATCH amendment — version references only, no principles changed).

- [X] T069 [P] Update Spring Boot version in `.specify/memory/constitution.md` Technology Stack table: change `Spring Boot 3.0.0` to `Spring Boot 4.0.5`; also update Spring Framework to `7.x` and Spring Security to `7.x` (both are managed by the SB 4.0.5 BOM)
- [X] T070 [P] Update JJWT version in `.specify/memory/constitution.md` Technology Stack table: change `JJWT 0.11.5` to `JJWT 0.12.6`
- [X] T071 [P] Update MySQL Connector/J version in `.specify/memory/constitution.md` Technology Stack table: change `MySQL Connector/J 8.0.31` to `MySQL Connector/J 9.6.0`
- [X] T072 [P] Update MapStruct version in `.specify/memory/constitution.md` Technology Stack table: change `MapStruct 1.6.0` to `MapStruct 1.6.3`
- [X] T073 [P] Update SpringDoc OpenAPI version in `.specify/memory/constitution.md` Technology Stack table: change `SpringDoc OpenAPI 2.0.2` to `SpringDoc OpenAPI 3.0.2`
- [X] T074 [P] Update Lombok version in `.specify/memory/constitution.md` Technology Stack table: change `Lombok 1.18.42` to `Lombok 1.18.44`
- [X] T075 Update the version field in `.specify/memory/constitution.md` from `1.0.1` to `1.0.2` and add a PATCH changelog entry: `v1.0.2 (PATCH): Technology Stack table updated to reflect Spring Boot 4.0.5 upgrade — Spring Boot 3.0.0→4.0.5, JJWT 0.11.5→0.12.6, MySQL Connector/J 8.0.31→9.6.0, MapStruct 1.6.0→1.6.3, SpringDoc 2.0.2→3.0.2, Lombok 1.18.42→1.18.44`
- [X] T076 [P] Update `CLAUDE.md` Active Technologies section: change `Spring Boot 3.0.0` to `Spring Boot 4.0.5`, `Spring Framework 7.x (managed)`, `Spring Security 7.x (managed)` — these should already be partially updated from an earlier session but verify they match the final versions
- [X] T077 [P] Update `CLAUDE.md` technology stack table rows: update the Framework row to `Spring Boot 4.0.5`, Security row to `Spring Security + JJWT 0.12.6`, DTO Mapping row to `MapStruct 1.6.3`, API Docs row to `SpringDoc OpenAPI (Swagger UI) 3.0.2`; leave Database row as `MySQL 8.0.31` (server version unchanged — only the driver changed)
- [X] T078 Run dependency tree comparison: `mvn dependency:tree -Doutput=/tmp/after-upgrade-deps.txt` then `diff /tmp/before-upgrade-deps.txt /tmp/after-upgrade-deps.txt`; review the diff for any transitive dependency version changes that are NOT in the planned upgrade matrix from `specs/007-spring-boot-upgrade/research.md` §2; if unexpected new artifacts or major version bumps appear (e.g., a transitive library jumping from 1.x to 3.x), investigate whether they could affect runtime behavior and add a comment in `pom.xml` above the relevant dependency if an explicit version pin is needed
- [X] T079 Commit documentation changes: `git add .specify/memory/constitution.md CLAUDE.md && git commit -m "docs: update constitution and CLAUDE.md to reflect Spring Boot 4.0.5 upgrade"`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Phase 2**: N/A — no foundational tasks
- **User Story 1 Sub-Phase A (T005–T031)**: Depends on Phase 1 completion
- **User Story 1 Sub-Phase B (T032–T037)**: Depends on Sub-Phase A being committed (T031)
- **User Story 2 (T038–T045)**: Depends on Sub-Phase B compile check (T036–T037)
- **User Story 3 (T046–T054)**: Depends on US2 completion (T045) — application must be running and healthy
- **User Story 4 (T055–T068)**: Depends on US3 completion (T054) — security must be verified first
- **Polish (T069–T079)**: Depends on US4 completion (T068) — only document what is validated

### User Story Dependencies

- **US1 (P1)**: Depends only on Phase 1 baseline verification
- **US2 (P2)**: Depends on US1 Sub-Phase B compile check — cannot validate startup before the code compiles
- **US3 (P2)**: Depends on US2 — cannot test JWT auth without the application running and healthy
- **US4 (P3)**: Depends on US3 — cannot test full API regression without confirming security is intact; includes cache behavior (FR-008), cart/order domain smoke tests, and full packaging validation

### Within Each Phase: Parallel Opportunities

- **Sub-Phase A parallel group**: T006, T007, T008, T009, T010, T011, T012, T013 can all be applied concurrently (all modify different lines of `pom.xml` with no ordering dependency between them)
- **Sub-Phase A sequential**: T005 (parent version) → {T006–T013} → T014 (migrator) → T015 (application.yml) → {T016–T027} (JwtService) → T028 (compile check)
- **Polish phase parallel group**: T069, T070, T071, T072, T073, T074, T076, T077 can all be applied concurrently (each updates a different row/section of different files); T078 (dep tree comparison) must run after `mvn dependency:tree` output from T002 exists, and T079 (commit) after all parallel tasks are done

---

## Parallel Example: Sub-Phase A pom.xml Changes

```text
# All of these can be applied in the same edit pass (different lines, no dependency):
T006: ${org.mapstruct.version}: 1.6.0 → 1.6.3
T007: ${maven.version}: 3.13.0 → 3.15.0
T008: ${lombok.version}: 1.18.42 → 1.18.44
T009: mysql-connector-j: 8.0.31 → 9.6.0
T010: jjwt-api: 0.11.5 → 0.12.6
T011: jjwt-impl: 0.11.5 → 0.12.6
T012: jjwt-jackson: 0.11.5 → 0.12.6
T013: springdoc: 2.0.2 → 2.8.16

# Must be done after the above in a separate pass:
T005: parent version: 3.0.0 → 3.5.9
T014: add properties-migrator block
```

---

## Implementation Strategy

### MVP Scope (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T004)
2. Complete US1 Sub-Phase A: SB 3.5.9 + JJWT + MySQL + MapStruct + Lombok + SpringDoc 2.8.16 + application.yml + JwtService (T005–T031)
3. **STOP and validate**: `mvn clean compile` succeeds, app starts on SB 3.5.9
4. Continue US1 Sub-Phase B: SB 4.0.5 + SpringDoc 3.0.2 + Jackson 3 (T032–T037)
5. **VALIDATE**: `mvn clean package -DskipTests` succeeds — US1 independent test criterion met

### Incremental Delivery

1. US1 → compile checkpoint → commit Phase 1 → advance to 4.0.5 → compile checkpoint
2. US2 → application starts, health UP
3. US3 → security smoke tests pass
4. US4 → full regression pass + Swagger UI
5. Polish → constitution + CLAUDE.md updated + final commit

---

## Notes

- `[P]` tasks within Sub-Phase A pom.xml section can all be applied in a single editor pass since they modify different lines with no ordering dependency between each other
- The two commits (T031, T068) mark the phase boundaries: SB 3.5.9 validated and SB 4.0.5 validated
- If `mvn spring-boot:run` hangs at startup, press Ctrl+C and check startup log — most likely cause is database or Redis not running
- The `spring-boot-properties-migrator` added in T014 is a safety net only — research.md §3.7 confirms no `application.yml` properties need renaming for this project; remove it in T035 regardless of whether it reported any warnings
- Do not skip T028 (intermediate compile check) — the most expensive bugs in this upgrade are JJWT compile errors that only surface at compile time, not at runtime
