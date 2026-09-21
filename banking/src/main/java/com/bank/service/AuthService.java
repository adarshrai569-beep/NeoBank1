package com.bank.service;

import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.dto.RegistrationResponse;

public interface AuthService {

    RegistrationResponse register(RegisterRequest request);

    String login(LoginRequest request);
}
