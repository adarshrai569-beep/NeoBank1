package com.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_audit_log", indexes = {
    @Index(name = "idx_audit_status", columnList = "responseStatus"),
    @Index(name = "idx_audit_timestamp", columnList = "eventTimestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false)
    private int responseStatus;

    @Column(nullable = false)
    private long executionTimeMs;

    private Long actingUserId;

    @Column(nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
    }
}
