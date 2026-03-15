# Quickstart: App-Wide Redis Caching

**Feature**: 002-redis-caching

## Prerequisites

- Docker and Docker Compose installed
- Maven 3.x installed
- JDK 17

---

## Step 1: Start Redis (and MySQL) via Docker Compose

```bash
docker compose up -d
```

This starts:
- MySQL on port `3306`
- Redis Stack on port `6379` (Redis protocol) and port `8001` (RedisInsight UI)

To verify Redis is running:

```bash
docker compose logs redis
```

---

## Step 2: Open RedisInsight (cache inspector)

Navigate to [http://localhost:8001](http://localhost:8001) in your browser.
Add a connection to `localhost:6379` to browse and inspect cached values as JSON.

---

## Step 3: Run the application

```bash
mvn spring-boot:run
```

The app connects to Redis via `localhost:6379` (configured in `application.yml`).

---

## Step 4: Verify caching is working

### Check cache hits in logs

With `logging.level.org.springframework.cache=DEBUG` set, you will see lines like:

```
DEBUG o.s.cache.interceptor.CacheInterceptor - Computed cache key '...' for operation ...
DEBUG o.s.cache.interceptor.CacheInterceptor - Cache entry for key '...' found in cache 'products'
```

The first request logs no "found in cache" line (cache miss → DB hit).
The second identical request logs "found in cache" (cache hit → no DB query).

### Confirm via SQL logs

The datasource proxy logger will show SQL queries on cache misses but NOT on cache hits:

```
# First request: SQL appears
Name:dataSource, Time:3, Success:True, Type:Prepared, Batch:False, ...

# Second request: no SQL — served from cache
```

### Inspect keys in RedisInsight

After a few requests, open RedisInsight and browse keys. They will appear as:

```
products::3fa85f64-5717-4562-b3fc-2c963f66afa6
categories::all
carts::7d8b9c12-...
```

Values are stored as human-readable JSON.

---

## Step 5: Verify cache fallback (optional)

Stop Redis while the app is running:

```bash
docker compose stop redis
```

Make a request to any cached endpoint. The app MUST:
1. Log a WARN about the cache error
2. Return a successful response (served from DB)
3. Return HTTP 200, not 500

Restart Redis afterwards:

```bash
docker compose start redis
```

---

## Troubleshooting

### SerializationException: Java 8 date/time type `java.time.Instant` not supported

If you see this error when caching entities:

```
org.springframework.data.redis.serializer.SerializationException: Could not write JSON: Java 8 date/time type `java.time.Instant` not supported by default
```

**Solution**:

1. Add the Jackson JSR310 module dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

2. Configure `ObjectMapper` with `JavaTimeModule` in `CacheConfig.java`:

```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
```

This enables serialization of `java.time` types (`Instant`, `LocalDateTime`, etc.) stored in your entities.
