package com.maatricare.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final int maximumFailures;
    private final Duration lockDuration;

    public LoginAttemptService(@Value("${security.login-protection.maximum-failures:5}") int maximumFailures,
            @Value("${security.login-protection.lock-minutes:15}") long lockMinutes) {
        this.maximumFailures = maximumFailures;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
    }

    public boolean isBlocked(String key) {
        AttemptState state = attempts.get(key);
        if (state == null) return false;
        if (state.blockedUntil() != null && state.blockedUntil().isAfter(Instant.now())) return true;
        if (state.blockedUntil() != null) attempts.remove(key, state);
        return false;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (ignored, current) -> {
            int failures = current == null || current.blockedUntil() != null ? 1 : current.failures() + 1;
            Instant blockedUntil = failures >= maximumFailures ? Instant.now().plus(lockDuration) : null;
            return new AttemptState(failures, blockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    public long retryAfterSeconds(String key) {
        AttemptState state = attempts.get(key);
        if (state == null || state.blockedUntil() == null) return 0;
        return Math.max(1, Duration.between(Instant.now(), state.blockedUntil()).toSeconds());
    }

    private record AttemptState(int failures, Instant blockedUntil) {}
}