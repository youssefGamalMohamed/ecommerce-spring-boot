package com.app.ecommerce.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
        log.warn("Redis cache get failed for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
        log.warn("Redis cache put failed for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
        log.warn("Redis cache evict failed for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
        log.warn("Redis cache clear failed for cache '{}': {}", cache.getName(), exception.getMessage());
    }
}
