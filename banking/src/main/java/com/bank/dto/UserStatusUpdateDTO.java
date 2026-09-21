package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "User activation status update")
@Getter
@Setter
public class UserStatusUpdateDTO {

    @Schema(description = "Whether the user is active", example = "true")
    private Boolean isActive;
}
