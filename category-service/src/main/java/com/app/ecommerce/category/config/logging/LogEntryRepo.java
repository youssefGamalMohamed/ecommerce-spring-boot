package com.app.ecommerce.category.config.logging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LogEntryRepo extends JpaRepository<LogEntry, Long> {
    LogEntry findByCorrelationId(String correlationId);
}
