package com.bank.service;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    // Store OTP with expiry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public String generateAndStoreOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        String otp = generateOtp();
        otpStore.put(email, new OtpEntry(otp, LocalDateTime.now().plusMinutes(5)));

        return otp;
    }

    public void resetPassword(String email, String otp, String newPassword) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            throw new RuntimeException("No OTP requested for this email");
        }

        if (LocalDateTime.now().isAfter(entry.expiry)) {
            otpStore.remove(email);
            throw new RuntimeException("OTP has expired");
        }

        if (!entry.otp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);

        otpStore.remove(email);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private record OtpEntry(String otp, LocalDateTime expiry) {}
}
