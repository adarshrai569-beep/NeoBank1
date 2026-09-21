package com.bank.controller;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public User getProfile(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }

    // ✅ ADD this endpoint that matches your frontend call
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(Authentication auth, @RequestBody Map<String, String> updates) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        // ✅ Update only the fields sent from frontend
        if (updates.containsKey("fullName")) {
            user.setFullName(updates.get("fullName"));
        }
        if (updates.containsKey("mobile")) {
            user.setMobile(updates.get("mobile"));
        }
        if (updates.containsKey("address")) {
            user.setAddress(updates.get("address"));
        }
        
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    // ✅ Keep your existing endpoint too
    @PutMapping("/me")
    public User updateProfile(Authentication auth, @RequestBody User updated) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        user.setFullName(updated.getFullName());
        return userRepository.save(user);
    }
}