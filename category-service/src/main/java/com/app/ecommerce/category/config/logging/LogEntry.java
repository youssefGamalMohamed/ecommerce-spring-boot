package com.app.ecommerce.category.config.logging;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correlationId;

    @Column(columnDefinition = "json")
    private String request;

    @Column(columnDefinition = "json")
    private String response;

    @Column(name = "request_arrival_time")
    private LocalDateTime requestArrivalTime;

    @Column(name = "request_finish_time")
    private LocalDateTime requestFinishTime;
}
