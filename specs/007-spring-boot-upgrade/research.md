# Research: Spring Boot 2026 Dependency Upgrade

**Feature**: 007-spring-boot-upgrade
**Date**: 2026-04-06
**Status**: Complete — all unknowns resolved

---

## 1. Upgrade Path Decision

### Decision
Adopt a **two-phase incremental upgrade**:

- **Phase 1**: Spring Boot `3.0.0` → `3.5.9` (latest stable 3.x)
- **Phase 2**: Spring Boot `3.5.9` → `4.0.5` (latest stable overall, April 2026)

### Rationale
The official Spring Boot migration guide mandates upgrading to the latest 3.x before moving to 4.0.
This reduces risk by separating the known 3.x deprecation fixes from the Spring Boot 4.0 breaking
changes (Jackson 3 group ID change, modular structure). Testing is done after each phase.

### Alternatives Considered
- **Direct jump 3.0.0 → 4.0.5**: Not recommended — skips intermediate deprecation fixes, harder to
  debug failures since all changes land at once.
- **Stay on 3.5.x permanently**: Discarded — user explicitly requested "2026 latest stable".

---

## 2. Target Version Matrix

| Dependency | Current | Phase 1 Target | Phase 2 Target | Breaking Changes? |
|---|---|---|---|---|
| Spring Boot Parent | 3.0.0 | **3.5.9** | **4.0.5** | Yes (see below) |
| Java | 17 | 17 (no change) | 17 (no change) | No — SB 4.0 min is 17 |
| JJWT (jjwt-api/impl/jackson) | 0.11.5 | **0.12.6** | 0.12.6 (no change) | Yes — full API rename |
| MySQL Connector/J | 8.0.31 | **9.6.0** | 9.6.0 (no change) | Minor |
| MapStruct | 1.6.0 | **1.6.3** | 1.6.3 (no change) | No |
| Lombok | 1.18.42 | **1.18.44** | 1.18.44 (no change) | No |
| SpringDoc OpenAPI | 2.0.2 | **2.8.16** | **3.0.2** | 3.x is major version |
| Maven Compiler Plugin | 3.13.0 | **3.15.0** | 3.15.0 (no change) | No |
| lombok-mapstruct-binding | 0.2.0 | 0.2.0 (no change) | 0.2.0 | No |
| jackson-datatype-jsr310 | (managed) | (managed) | Group ID changes | Yes |

**Version sources**: Maven Central, spring.io/blog, github.com/spring-projects/spring-boot/releases,
github.com/springdoc/springdoc-openapi/releases (verified April 2026).

---

## 3. Breaking Change Inventory (per dependency)

### 3.1 Spring Boot 3.0.0 → 3.5.9 (Phase 1)

#### Hibernate Dialect (affects `application.yml`)
- `org.hibernate.dialect.MySQL8Dialect` was deprecated in Hibernate 6.2 and removed by Hibernate 6.4
- Spring Boot 3.2+ manages Hibernate 6.4+
- **Fix**: Remove `spring.jpa.database-platform` from `application.yml`; Spring Boot auto-detects the
  correct MySQL dialect from the connector on the classpath. Alternatively use `MySQLDialect`.

#### Spring Security Lambda DSL (no action needed)
- Spring Security 6 (shipped in SB 3.0) already mandated lambda DSL
- `SecurityConfig.java` already uses `AbstractHttpConfigurer::disable`, `authorizeHttpRequests(auth -> ...)`,
  `sessionManagement(session -> ...)` — **no changes required**

#### Property Renames (SB 3.x accumulated)
- Handled by adding `spring-boot-properties-migrator` dependency temporarily to detect renames at startup
- Remove it after migration is complete
- No known property renames from the `application.yml` review affect this project directly

#### Spring Boot 3.2+ Test Changes (no action needed)
- `@MockBean` / `@SpyBean` remained valid in 3.x
- `EcommerceApplicationTests` only has `contextLoads()` — no test breaking changes

---

### 3.2 JJWT 0.11.5 → 0.12.6 (Phase 1)

This is the most impactful code change. `JwtService.java` uses deprecated APIs from 0.11.x that are
**removed** in 0.12.x (not merely deprecated — they do not compile).

| Location in JwtService.java | Old API (0.11.x) | New API (0.12.x) |
|---|---|---|
| `getSignInKey()` return type | `Key` | `SecretKey` |
| Token builder claims | `.setClaims(extraClaims)` | `.claims(extraClaims)` |
| Token builder subject | `.setSubject(username)` | `.subject(username)` |
| Token builder issued-at | `.setIssuedAt(date)` | `.issuedAt(date)` |
| Token builder expiration | `.setExpiration(date)` | `.expiration(date)` |
| Token builder sign | `.signWith(key, SignatureAlgorithm.HS256)` | `.signWith(key)` (algorithm inferred from SecretKey) |
| Parser builder | `Jwts.parserBuilder()` | `Jwts.parser()` |
| Parser signing key | `.setSigningKey(key)` | `.verifyWith((SecretKey) key)` |
| Parse JWT | `.parseClaimsJws(token)` | `.parseSignedClaims(token)` |
| Extract body | `.getBody()` | `.getPayload()` |
| Remove import | `import io.jsonwebtoken.SignatureAlgorithm` | (remove entirely) |
| Add import | — | `import java.security.interfaces.SecretKey` (via `Keys.hmacShaKeyFor`) |

**Note**: `Keys.hmacShaKeyFor(bytes)` returns a `SecretKey`, not just `Key` — updating the return type
from `Key` to `SecretKey` makes the cast in `.verifyWith()` unnecessary and cleaner.

---

### 3.3 MySQL Connector/J 8.0.31 → 9.6.0 (Phase 1)

- **Major version jump** (8.x → 9.x), but backward-compatible for standard usage
- JDBC URL format `jdbc:mysql://localhost:3306/ecommerce` remains valid
- Driver class `com.mysql.cj.jdbc.Driver` remains valid (unchanged since 8.x)
- `scope: runtime` stays correct
- **Potential issue**: `allowPublicKeyRetrieval` and `useSSL` JDBC URL flags may behave differently
  in 9.x for connections without SSL. Local dev connections (localhost) are unaffected.
- **No code changes required**; monitor connection pool behavior at runtime

---

### 3.4 MapStruct 1.6.0 → 1.6.3 (Phase 1)

- Patch release series; no breaking changes
- MapStruct processor in `annotationProcessorPaths` must be kept in sync with the main artifact
- **No code changes required**

---

### 3.5 Lombok 1.18.42 → 1.18.44 (Phase 1)

- Patch release; no breaking changes
- `lombok-mapstruct-binding` `0.2.0` is already compatible — no change needed
- **No code changes required**

---

### 3.6 SpringDoc OpenAPI 2.0.2 → 2.8.16 (Phase 1) then → 3.0.2 (Phase 2)

**Phase 1 (2.0.2 → 2.8.16):**
- Fully backward-compatible within 2.x for annotation-based controllers
- `OpenApiDocumentationConfig.java` uses `io.swagger.v3.oas.models.*` — unchanged
- `application.yml` `springdoc.swagger-ui.path` and `springdoc.api-docs.path` — unchanged
- **No code changes required**

**Phase 2 (2.8.16 → 3.0.2):**
- 3.x is SpringDoc's dedicated release line for Spring Boot 4.x
- Same `org.springdoc:springdoc-openapi-starter-webmvc-ui` artifact ID — only the version number changes
- `io.swagger.v3.oas.models.*` imports remain unchanged (swagger-models library is unchanged)
- Constitution rule VIII (`@SecurityRequirements` on public endpoints) remains valid and supported
- **No code changes required**; only version number update in pom.xml

---

### 3.7 Spring Boot 3.5.9 → 4.0.5 (Phase 2)

#### Jackson 3 Dependency (affects `pom.xml`)
- Spring Boot 4.0 ships with Jackson 3 (managed coordinates: `tools.jackson.*`)
- Jackson 2 support "ships in deprecated form" in SB 4.0 — existing `com.fasterxml.jackson.*` artifacts
  are available but deprecated
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` in `pom.xml` (currently version-managed):
  - **Option A (Minimal change)**: Leave as-is; SB 4.0 BOM still provides Jackson 2 compatibility shim
  - **Option B (Clean)**: Change to `tools.jackson.datatype:jackson-datatype-jsr310` (SB 4.0 BOM manages version)
  - **Decision**: Use Option B for a clean upgrade; remove the deprecated Jackson 2 reference

#### Property Renames in Spring Boot 4.0 (affects `application.yml`)
| Old Property | New Property | Present in project? |
|---|---|---|
| `spring.dao.exceptiontranslation.enabled` | `spring.persistence.exceptiontranslation.enabled` | No |
| `management.tracing.enabled` | `management.tracing.export.enabled` | No |
| `spring.session.redis.*` | `spring.session.data.redis.*` | No |
- **No property renames affect this project's `application.yml`**

#### Test API Changes (affects `EcommerceApplicationTests.java`)
- `@MockBean` → `@MockitoBean` (Spring Boot 4.0 removed the old annotation)
- `@SpyBean` → `@MockitoSpyBean`
- `@SpringBootTest` no longer auto-configures MockMVC — need `@AutoConfigureMockMvc` if used
- `EcommerceApplicationTests` only has `contextLoads()` with `@SpringBootTest` — **no changes required**

#### Hibernate 7.1 (managed by Spring Boot 4.0 BOM)
- Hibernate dialect already removed from `application.yml` in Phase 1
- Spring Boot 4.0 uses Hibernate 7.1 — auto-detection unchanged
- `@Version` on entities, `@Transactional`, and `BaseEntity` auditing annotations remain valid
- **No code changes required** (beyond Phase 1 dialect removal)

#### Maven Compiler Plugin 3.13.0 → 3.15.0 (Phase 1)
- Minor version bump, fully backward-compatible
- `annotationProcessorPaths` configuration (MapStruct + Lombok) unchanged
- **No configuration changes required**

---

## 4. SpringDoc Compatibility Matrix (reference)

| SpringDoc version | Spring Boot version |
|---|---|
| 2.0.x | 3.0.x |
| 2.1.x – 2.4.x | 3.1.x |
| 2.5.x – 2.6.x | 3.2.x |
| 2.7.x | 3.3.x |
| 2.8.x | 3.4.x – 3.5.x |
| 3.0.x | 4.0.x |

---

## 5. Files Requiring Changes

| File | Phase | Change Type |
|---|---|---|
| `pom.xml` | 1 | Version bumps (SB, JJWT, MySQL, MapStruct, Lombok, SpringDoc 2.8.16, plugin) |
| `pom.xml` | 2 | SB → 4.0.5, SpringDoc → 3.0.2, jackson group ID migration |
| `JwtService.java` | 1 | JJWT 0.12.x API migration (builder + parser) |
| `application.yml` | 1 | Remove deprecated Hibernate dialect property |
| `EcommerceApplicationTests.java` | 2 | No changes needed (contextLoads only) |
| `SecurityConfig.java` | — | No changes needed (already uses lambda DSL) |
| `OpenApiDocumentationConfig.java` | — | No changes needed |

---

## 6. Risk Assessment

| Risk | Likelihood | Mitigation |
|---|---|---|
| JJWT compilation failure | High (APIs removed, not deprecated) | Full migration table in §3.2 |
| Hibernate dialect startup error | Medium (if not removed) | Remove property in Phase 1 |
| MySQL 9.x connection behavior | Low | Test at runtime; URL/driver unchanged |
| SpringDoc 3.x Swagger UI breakage | Low | Same artifact ID, same package, same YAML config |
| Jackson 3 serialization difference | Low | Existing test covers context load; monitor response shapes manually |
| Spring Boot 4.0 test runner changes | Low | Only `contextLoads()` test exists — no impact |
