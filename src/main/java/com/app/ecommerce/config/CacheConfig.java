package com.app.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.ttl.products:600000}")
    private long productsTtl;



    @Value("${cache.ttl.categories:1800000}")
    private long categoriesTtl;

    @Value("${cache.ttl.carts:300000}")
    private long cartsTtl;

    @Value("${cache.ttl.orders:900000}")
    private long ordersTtl;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMillis(productsTtl)));

        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofMillis(categoriesTtl)));
        cacheConfigurations.put("carts", defaultConfig.entryTtl(Duration.ofMillis(cartsTtl)));
        cacheConfigurations.put("orders", defaultConfig.entryTtl(Duration.ofMillis(ordersTtl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new RedisCacheErrorHandler();
    }

    public static class RedisCacheErrorHandler implements CacheErrorHandler {
        @Override
        public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
            org.slf4j.LoggerFactory.getLogger(CacheConfig.class)
                    .warn("Redis cache get failed for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
            org.slf4j.LoggerFactory.getLogger(CacheConfig.class)
                    .warn("Redis cache put failed for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
            org.slf4j.LoggerFactory.getLogger(CacheConfig.class)
                    .warn("Redis cache evict failed for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
            org.slf4j.LoggerFactory.getLogger(CacheConfig.class)
                    .warn("Redis cache clear failed for cache '{}': {}", cache.getName(), exception.getMessage());
        }
    }
}