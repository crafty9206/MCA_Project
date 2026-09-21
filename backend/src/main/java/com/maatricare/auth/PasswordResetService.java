package com.maatricare.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@Service
public class PasswordResetService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createToken(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase()).orElse(null);
        if (user == null) return null;
        tokenRepository.deleteByUserId(user.getId());
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenRepository.save(new PasswordResetToken(user, hash(token), Instant.now().plus(30, ChronoUnit.MINUTES)));
        return token;
    }

    @Transactional
    public void resetPassword(String token, String password) {
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hash(token))
                .filter(candidate -> candidate.getExpiresAt().isAfter(Instant.now()))
            .orElseThrow(InvalidPasswordResetTokenException::new);
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.deleteByUserId(user.getId());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}