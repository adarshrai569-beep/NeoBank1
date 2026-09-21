package com.bank.repository;

import com.bank.entity.SystemAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {

    Page<SystemAuditLog> findByEventTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<SystemAuditLog> findByResponseStatusBetweenAndEventTimestampBetween(
            int statusFrom, int statusTo, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SystemAuditLog s WHERE s.responseStatus >= 400 AND s.eventTimestamp >= :since")
    long countErrorsSince(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(s.executionTimeMs) FROM SystemAuditLog s WHERE s.eventTimestamp >= :since")
    Double averageExecutionTimeSince(@Param("since") LocalDateTime since);
}
