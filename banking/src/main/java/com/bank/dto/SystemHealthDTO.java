package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "System health status snapshot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDTO {

    @Schema(description = "Database connectivity status", example = "UP")
    private String dbStatus;

    @Schema(description = "Active sessions count", example = "0")
    private Long activeSessions;

    @Schema(description = "Uptime in seconds", example = "3600")
    private Long serverUptimeSeconds;
}
