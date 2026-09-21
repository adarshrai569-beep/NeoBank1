package com.bank.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOG");

    public void logAdminAction(Long adminId, String action, String targetType, Long targetId) {
        logger.info("AUDIT adminId={} action={} targetType={} targetId={} timestamp={}",
                adminId, action, targetType, targetId, Instant.now());
    }
}
