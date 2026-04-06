# Developer Quickstart: Spring Boot 2026 Dependency Upgrade

**Branch**: `007-spring-boot-upgrade`
**Estimated effort**: 2–4 hours
**Java version required**: 17+ (no JVM upgrade needed)

---

## Before You Begin

Ensure your local environment is running:
- MySQL 8 on `localhost:3306` (database `ecommerce`)
- Redis on `localhost:6379`

Confirm you are on the correct branch:
```bash
git branch
# should show: * 007-spring-boot-upgrade
```

---

## Phase 1: Upgrade to Spring Boot 3.5.9

### Step 1 — Update `pom.xml`

Make these changes in `pom.xml`:

**1a. Update the Spring Boot parent:**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.9</version>  <!-- was 3.0.0 -->
    <relativePath />
</parent>
```

**1b. Update version properties:**
```xml
<properties>
    <java.version>17</java.version>
    <org.mapstruct.version>1.6.3</org.mapstruct.version>      <!-- was 1.6.0 -->
    <maven.version>3.15.0</maven.version>                      <!-- was 3.13.0 -->
    <lombok.mapstruct.binding>0.2.0</lombok.mapstruct.binding>
    <lombok.version>1.18.44</lombok.version>                   <!-- was 1.18.42 -->
</properties>
```

**1c. Update explicit dependency versions:**
```xml
<!-- MySQL Connector: was 8.0.31 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.6.0</version>
    <scope>runtime</scope>
</dependency>

<!-- JJWT: all three artifacts, was 0.11.5 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- SpringDoc: was 2.0.2 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.16</version>
</dependency>
```

**1d. Add the temporary properties migrator** (anywhere in `<dependencies>`):
```xml
<!-- TEMPORARY: Remove after upgrade is complete -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-properties-migrator</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### Step 2 — Migrate `JwtService.java` to JJWT 0.12.x

Open `src/main/java/com/app/ecommerce/shared/security/JwtService.java`.

**Replace the imports section:**
```java
// REMOVE:
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;

// ADD:
import javax.crypto.SecretKey;
```

**Update `generateToken()` method body:**
```java
// BEFORE:
return Jwts
        .builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();

// AFTER:
return Jwts
        .builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey())
        .compact();
```

**Update `extractAllClaims()` method body:**
```java
// BEFORE:
return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();

// AFTER:
return Jwts
        .parser()
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
```

**Update `getSignInKey()` return type:**
```java
// BEFORE:
private Key getSignInKey() {

// AFTER:
private SecretKey getSignInKey() {
```

---

### Step 3 — Fix `application.yml`

Remove the deprecated `database-platform` property from the `spring.jpa` block:

```yaml
spring:
  jpa:
    # REMOVE this line: database-platform: org.hibernate.dialect.MySQL8Dialect
    generate-ddl: true
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
```

---

### Step 4 — Validate Phase 1

```bash
# 1. Compile
mvn clean compile

# 2. Start the application and watch for migrator output
mvn spring-boot:run
# Look for "[PropertiesMigrationListener]" lines — address any reported renames
# Application should print: Started EcommerceApplication in X seconds

# 3. Health check (in a separate terminal)
curl http://localhost:8081/ecommerce/api/v1/actuator/health

# 4. Run tests
mvn test

# 5. Smoke tests
# Public endpoint (no token):
curl http://localhost:8081/ecommerce/api/v1/products

# Protected endpoint (no token — expect 401):
curl -i http://localhost:8081/ecommerce/api/v1/orders

# Login (get token):
curl -X POST http://localhost:8081/ecommerce/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"password"}'
```

If all pass, commit Phase 1:
```bash
git add pom.xml src/main/java/com/app/ecommerce/shared/security/JwtService.java src/main/resources/application.yml
git commit -m "chore(deps): upgrade to Spring Boot 3.5.9 + JJWT 0.12.6 + MySQL 9.6.0"
```

---

## Phase 2: Upgrade to Spring Boot 4.0.5

### Step 5 — Update `pom.xml` for Spring Boot 4.0

**5a. Update the Spring Boot parent:**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>  <!-- was 3.5.9 -->
    <relativePath />
</parent>
```

**5b. Update SpringDoc to the Spring Boot 4.x line:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.2</version>  <!-- was 2.8.16 -->
</dependency>
```

**5c. Update Jackson JSR310 to Jackson 3 group ID:**
```xml
<!-- BEFORE: -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- AFTER: -->
<dependency>
    <groupId>tools.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

**5d. Remove the temporary properties-migrator dependency** (added in Step 1d).

---

### Step 6 — Validate Phase 2

```bash
# 1. Compile
mvn clean compile

# 2. Start the application
mvn spring-boot:run
# Watch for NoClassDefFoundError related to Jackson — if seen, check Step 5c

# 3. Full validation suite
curl http://localhost:8081/ecommerce/api/v1/actuator/health
mvn test

# 4. Smoke tests (repeat Phase 1 smoke tests)
# Public: GET /products, GET /categories
# Protected: GET /orders → 401
# Auth: POST /auth/login → JWT
# Swagger UI: open http://localhost:8081/ecommerce/api/v1/swagger-ui in browser
#   → Login flow should work (POST /auth/login visible without padlock)

# 5. Full build
mvn clean install -DskipTests
```

If all pass, commit Phase 2:
```bash
git add pom.xml
git commit -m "chore(deps): upgrade to Spring Boot 4.0.5 + SpringDoc 3.0.2 + Jackson 3"
```

---

## Phase 3: Post-Upgrade Cleanup

### Step 7 — Update constitution and CLAUDE.md

Update the Technology Stack tables in:
- `.specify/memory/constitution.md` (PATCH version bump)
- `CLAUDE.md` (Active Technologies table)

New versions to record:

| Concern | New Version |
|---|---|
| Spring Boot | 4.0.5 |
| JJWT | 0.12.6 |
| MySQL Connector/J | 9.6.0 |
| MapStruct | 1.6.3 |
| SpringDoc OpenAPI | 3.0.2 |
| Lombok | 1.18.44 |

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---|---|---|
| Compile error on `setClaims` / `setSubject` | JJWT 0.12 API change | Follow Step 2 exactly |
| Compile error on `parserBuilder` | JJWT 0.12 API change | Follow Step 2 exactly |
| Startup: `Unsupported dialect class` | Old dialect property | Follow Step 3 |
| Startup: `NoClassDefFoundError: com/fasterxml/jackson` | Jackson 3 group ID | Follow Step 5c |
| 404 on `/swagger-ui` | SpringDoc config | Verify `springdoc.swagger-ui.path=/swagger-ui` in yml |
| 403 on public endpoints after startup | Spring Security config | Verify `SecurityConfig.java` unchanged |
| `contextLoads` test fails | Bean wiring broken | Check startup logs for the root cause |
