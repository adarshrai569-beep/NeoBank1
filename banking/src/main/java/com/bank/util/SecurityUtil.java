package com.bank.util;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest request;

    // ✅ Extract token from header
    private String getToken() {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or Invalid Authorization header");
        }

        return header.substring(7); // remove "Bearer "
    }

    // ✅ Get User ID
    public Long getUserId() {

        String token = getToken();

        Claims claims = jwtUtil.extractClaims(token);

        return claims.get("id", Long.class);
    }

    // ✅ Get Role
    public String getRole() {

        String token = getToken();

        Claims claims = jwtUtil.extractClaims(token);

        return claims.get("role", String.class);
    }

    // ✅ Get Email (subject)
    public String getEmail() {

        String token = getToken();

        Claims claims = jwtUtil.extractClaims(token);

        return claims.getSubject();
    }
}