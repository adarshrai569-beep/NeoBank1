package com.bank.config;

import com.bank.entity.SystemAuditLog;
import com.bank.repository.SystemAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final SystemAuditLogRepository auditLogRepository;

    @Around("execution(* com.bank.controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        int responseStatus = 200;
        String errorMessage = null;

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception ex) {
            responseStatus = 500;
            errorMessage = sanitizeMessage(ex.getMessage());
            throw ex;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveAuditLog(joinPoint, responseStatus, executionTime, errorMessage);
        }
    }

    private void saveAuditLog(ProceedingJoinPoint joinPoint, int status, long executionTime, String errorMessage) {
        try {
            String endpoint = joinPoint.getSignature().toShortString();
            String httpMethod = "UNKNOWN";
            Long userId = null;

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                endpoint = request.getRequestURI();
                httpMethod = request.getMethod();
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                try {
                    userId = Long.parseLong(auth.getName());
                } catch (NumberFormatException ignored) {}
            }

            SystemAuditLog log = SystemAuditLog.builder()
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .responseStatus(status)
                    .executionTimeMs(executionTime)
                    .actingUserId(userId)
                    .eventTimestamp(LocalDateTime.now())
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Never let audit logging break the main request
        }
    }

    private String sanitizeMessage(String message) {
        if (message == null) return null;
        // Never log passwords, tokens, or PII
        return message.replaceAll("(?i)(password|token|bearer|jwt)\\s*[:=]\\s*\\S+", "[REDACTED]")
                .substring(0, Math.min(message.length(), 500));
    }
}
