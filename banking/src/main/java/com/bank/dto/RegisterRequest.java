package com.bank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[1-9]\\d{9}$", message = "Invalid mobile number")
    private String mobile;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String accountType; // SAVINGS / CURRENT

    @NotBlank
 
    private String idType; // AADHAR / PAN

    @NotBlank
//    @Pattern(regexp = "^[1-9]\\d{11}$", message = "Invalid  ID number")
    private String idNumber;

    @NotBlank
    private String address;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}