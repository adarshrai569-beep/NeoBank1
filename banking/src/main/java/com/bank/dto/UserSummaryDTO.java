package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "User summary for admin listing")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {

    @Schema(description = "User id", example = "8")
    private Long id;

    @Schema(description = "Full name", example = "Aditi Rao")
    private String fullName;

    @Schema(description = "Email address", example = "aditi@neobank.in")
    private String email;

    @Schema(description = "Role name", example = "CUSTOMER")
    private String role;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Registration date")
    private LocalDateTime createdAt;
}
