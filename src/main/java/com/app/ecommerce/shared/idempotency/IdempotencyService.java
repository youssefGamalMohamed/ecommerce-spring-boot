package com.app.ecommerce.shared.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public Optional<IdempotencyRecord> findByKey(String key) {
        return idempotencyRepository.findByIdempotencyKey(key)
                .filter(record -> record.getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    public void store(String key, int httpStatus, Object responseBody) {
        try {
            String responseBodyJson = objectMapper.writeValueAsString(responseBody);
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .httpStatus(httpStatus)
                    .responseBody(responseBodyJson)
                    .build();
            idempotencyRepository.save(record);
            log.info("Stored idempotency record for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to store idempotency record: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredKeys() {
        try {
            idempotencyRepository.deleteByExpiresAtBefore(Instant.now());
            log.info("Cleaned up expired idempotency keys");
        } catch (Exception e) {
            log.error("Failed to cleanup expired idempotency keys: {}", e.getMessage());
        }
    }
}
