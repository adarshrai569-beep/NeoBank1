//package com.bank.service.impl;
//
//import com.bank.dto.LoginRequest;
//import com.bank.dto.RegisterRequest;
//import com.bank.dto.RegistrationResponse;
//import com.bank.entity.*;
//import com.bank.repository.AccountRepository;
//import com.bank.repository.UserRepository;
//import com.bank.service.AuthService;
//import com.bank.util.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//
//@Service
//@RequiredArgsConstructor
//public class AuthServiceImpl implements AuthService {
//
//    private final UserRepository userRepository;
//    private final AccountRepository accountRepository;
//    private final JwtUtil jwtUtil;
//    private final BCryptPasswordEncoder encoder;
//
//    @Override
//    public RegistrationResponse register(RegisterRequest req) {
//        
//        if (userRepository.existsByEmail(req.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//
//        // ✅ Check if ANY user exists, not just ADMIN role
//        long userCount = userRepository.count();
//        boolean isFirstUser = (userCount == 0);
//
//        String customerId = "CUST" + req.getFullName().trim().toUpperCase().charAt(0) + "" + System.currentTimeMillis() % 100000;
//        String accountNumber = "ACC" + System.currentTimeMillis();
//
//        User user = User.builder()
//                .fullName(req.getFullName())
//                .email(req.getEmail())
//                .mobile(req.getMobile())
//                .address(req.getAddress())
//                .accountType(req.getAccountType())
//                .idType(req.getIdType())
//                .idNumber(req.getIdNumber())
//                .customerId(customerId)
//                .passwordHash(encoder.encode(req.getPassword()))
//                .role(isFirstUser ? Role.ADMIN : Role.CUSTOMER)
//                .approved(isFirstUser) // First user auto-approved
//                .isActive(isFirstUser) // First user auto-activated
//                .build();
//
//        userRepository.save(user);
//
//        Account account = Account.builder()
//                .user(user)
//                .accountType(AccountType.valueOf(req.getAccountType()))
//                .accountNumber(accountNumber)
//                .balance(BigDecimal.ZERO)
//                .isActive(isFirstUser) // First user account active
//                .isFrozen(false) // First user account not frozen
//                .build();
//
//        accountRepository.save(account);
//
//        return new RegistrationResponse(customerId, accountNumber);
//    }
//
//
//    
//    @Override
//    public String login(LoginRequest request) {
//
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
//
//        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
//            throw new RuntimeException("Invalid credentials");
//        }
//
//        if (!user.getIsActive()) {
//            throw new RuntimeException("User disabled");
//        }
//
//        return jwtUtil.generateToken(
//                user.getId(),
//                user.getEmail(),
//                user.getRole().name()
//        );
//    }
//}





package com.bank.service.impl;

import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.dto.RegistrationResponse;
import com.bank.entity.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.LoginEventRepository;
import com.bank.repository.UserRepository;
import com.bank.service.AuthService;
import com.bank.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final LoginEventRepository loginEventRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @Override
    public RegistrationResponse register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // ✅ First user becomes ADMIN
        long userCount = userRepository.count();
        boolean isFirstUser = (userCount == 0);

        String customerId = "CUST" + req.getFullName().trim().toUpperCase().charAt(0)
                + System.currentTimeMillis() % 100000;

        String accountNumber = "ACC" + System.currentTimeMillis();

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .mobile(req.getMobile())
                .address(req.getAddress())
                .accountType(req.getAccountType())
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .customerId(customerId)
                .passwordHash(encoder.encode(req.getPassword()))
                .role(isFirstUser ? Role.ADMIN : Role.CUSTOMER)
                .approved(isFirstUser)
                .isActive(isFirstUser)
                .build();

        userRepository.save(user);

        Account account = Account.builder()
                .user(user)
                .accountType(AccountType.valueOf(req.getAccountType()))
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .isActive(isFirstUser)
                .isFrozen(false)
                .build();

        accountRepository.save(account);

        return new RegistrationResponse(customerId, accountNumber);
    }


    @Override
    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("User disabled");
        }

        loginEventRepository.save(LoginEvent.builder()
            .userId(user.getId())
            .loginAt(LocalDateTime.now())
            .build());

        // ✅ ✅ IMPORTANT FIX: Add ROLE_ prefix
        return jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                "ROLE_" + user.getRole().name()
        );
    }
}