package com.maatricare.notifications;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Set;

import com.maatricare.user.User;
import com.maatricare.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private static final Set<String> SAFE_PUSH_HOST_SUFFIXES = Set.of(
            "push.services.mozilla.com", "notify.windows.com", "push.apple.com");
    private final UserRepository userRepository;
    private final PushSubscriptionRepository subscriptionRepository;
    private final PushReminderRepository reminderRepository;
    private final PushNotificationService pushService;

    public PushController(UserRepository userRepository, PushSubscriptionRepository subscriptionRepository,
            PushReminderRepository reminderRepository, PushNotificationService pushService) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.reminderRepository = reminderRepository;
        this.pushService = pushService;
    }

    @GetMapping("/settings")
    public PushSettings settings(Authentication authentication) {
        User user = currentUser(authentication);
        return new PushSettings(pushService.isConfigured(), pushService.getPublicKey(),
                user.isPushNotificationsEnabled(), subscriptionRepository.countByUserId(user.getId()),
                user.getTimezoneId());
    }

    @PostMapping("/subscriptions")
    @Transactional
    public PushSettings saveSubscription(Authentication authentication,
            @Valid @RequestBody SubscriptionRequest request) {
        User user = currentUser(authentication);
        if (!pushService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Push notifications are not configured");
        }
        validateSubscription(request);
        String endpointHash = sha256(request.endpoint());
        PushSubscription subscription = subscriptionRepository.findByUserIdAndEndpointHash(user.getId(), endpointHash)
                .orElseGet(() -> new PushSubscription(user, request.endpoint(), endpointHash,
                        request.keys().p256dh(), request.keys().auth()));
        subscription.update(request.endpoint(), endpointHash, request.keys().p256dh(), request.keys().auth());
        subscriptionRepository.save(subscription);
        user.setPushNotificationsEnabled(true);
        userRepository.save(user);
        return settings(authentication);
    }

    @DeleteMapping("/subscriptions")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSubscriptions(Authentication authentication) {
        User user = currentUser(authentication);
        subscriptionRepository.deleteByUserId(user.getId());
        user.setPushNotificationsEnabled(false);
        userRepository.save(user);
    }

    @PostMapping("/snooze")
    @Transactional
    public void snooze(Authentication authentication, @Valid @RequestBody SnoozeRequest request) {
        User user = currentUser(authentication);
        PushReminder reminder = reminderRepository.findByUserIdAndDeduplicationKey(
                        user.getId(), request.deduplicationKey())
                .orElseThrow(() -> new NoSuchElementException("Reminder not found"));
        if ("CANCELLED".equals(reminder.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reminder is no longer active");
        }
        reminder.snoozeUntil(Instant.now().plusSeconds(60 * 60));
        reminderRepository.save(reminder);
    }

    @PostMapping("/snooze-action")
    @Transactional
    public void snoozeFromNotification(@Valid @RequestBody SnoozeActionRequest request) {
        String tokenHash = sha256(request.snoozeToken());
        PushReminder reminder = reminderRepository.findByDeduplicationKeyAndSnoozeTokenHash(
                        request.deduplicationKey(), tokenHash)
                .orElseThrow(() -> new NoSuchElementException("Reminder action is invalid or expired"));
        if (!"SENT".equals(reminder.getStatus()) || reminder.getSnoozeTokenExpiresAt() == null
            || !reminder.getSnoozeTokenExpiresAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reminder is no longer active");
        }
        reminder.snoozeUntil(Instant.now().plusSeconds(60 * 60));
        reminderRepository.save(reminder);
    }

    private void validateSubscription(SubscriptionRequest request) {
        try {
            URI endpoint = URI.create(request.endpoint());
            String host = endpoint.getHost();
            boolean allowedHost = host != null && (host.equals("fcm.googleapis.com")
                    || host.equals("fcmregistrations.googleapis.com")
                    || SAFE_PUSH_HOST_SUFFIXES.stream().anyMatch(suffix -> host.equals(suffix) || host.endsWith("." + suffix)));
            if (!"https".equalsIgnoreCase(endpoint.getScheme()) || !allowedHost
                    || endpoint.getUserInfo() != null || endpoint.getPort() != -1) {
                throw new IllegalArgumentException("Unsupported push endpoint");
            }
            if (Base64.getUrlDecoder().decode(request.keys().p256dh()).length != 65
                    || Base64.getUrlDecoder().decode(request.keys().auth()).length != 16) {
                throw new IllegalArgumentException("Invalid push subscription keys");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid browser push subscription");
        }
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }

    public record SubscriptionRequest(@NotBlank @Size(max = 2048) String endpoint,
            @jakarta.validation.constraints.NotNull @Valid SubscriptionKeys keys) {}
    public record SubscriptionKeys(@NotBlank @Size(max = 128) String p256dh,
            @NotBlank @Size(max = 128) String auth) {}
    public record SnoozeRequest(@NotBlank @Size(max = 300) String deduplicationKey) {}
        public record SnoozeActionRequest(@NotBlank @Size(max = 300) String deduplicationKey,
            @NotBlank @Size(min = 40, max = 64) String snoozeToken) {}
    public record PushSettings(boolean available, String publicKey, boolean enabled, long subscriptionCount,
            String timezoneId) {}
}