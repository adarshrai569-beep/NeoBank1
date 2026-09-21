package com.bank.controller;

import com.bank.dto.FinancialInsightsDTO;
import com.bank.service.InsightsService;
import com.bank.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@Tag(name = "Insights", description = "User financial insights endpoints")
public class InsightsController {

    private final InsightsService insightsService;
    private final SecurityUtil securityUtil;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user financial insights", description = "Returns income, expense, savings, and trend summary for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insights retrieved", content = @Content(schema = @Schema(implementation = FinancialInsightsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<FinancialInsightsDTO> getInsights(@PathVariable Long userId) {
        Long jwtUserId = securityUtil.getUserId();
        if (jwtUserId == null || !jwtUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(insightsService.getInsights(userId));
    }
}
