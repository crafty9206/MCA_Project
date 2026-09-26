package com.maatricare.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api")
public class ReportShareController {

    private final SecureRandom secureRandom = new SecureRandom();
    private final ReportShareRepository shareRepository;
    private final HealthcareReportController reportController;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ReportShareController(ReportShareRepository shareRepository,
            HealthcareReportController reportController, UserRepository userRepository, ObjectMapper objectMapper) {
        this.shareRepository = shareRepository;
        this.reportController = reportController;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/reports/shares")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedShare create(Authentication authentication,
            @RequestParam(defaultValue = "24") int expiresInHours,
            @RequestParam(defaultValue = "false") boolean includeJournal) {
        if (expiresInHours < 1 || expiresInHours > 168) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry must be between 1 and 168 hours");
        }
        User user = currentUser(authentication);
        HealthcareReportController.HealthcareReport report = reportController.pregnancySummaryFor(user, includeJournal);
        String token = token();
        OffsetDateTime createdAt = OffsetDateTime.now();
        try {
            ReportShare share = shareRepository.save(new ReportShare(user, hash(token),
                    objectMapper.writeValueAsString(report), includeJournal, createdAt,
                    createdAt.plusHours(expiresInHours)));
            return new CreatedShare(share.getId(), token, share.getExpiresAt(), includeJournal);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Report could not be shared");
        }
    }

    @GetMapping("/reports/shares")
    public List<ShareSummary> shares(Authentication authentication) {
        return shareRepository.findByUserIdOrderByCreatedAtDesc(currentUser(authentication).getId()).stream()
                .map(this::summary).toList();
    }

    @DeleteMapping("/reports/shares/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(Authentication authentication, @PathVariable UUID id) {
        ReportShare share = shareRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        share.revoke();
        shareRepository.save(share);
    }

    @GetMapping("/shared-reports/{token}")
    @Transactional
    public HealthcareReportController.HealthcareReport sharedReport(@PathVariable String token) {
        ReportShare share = shareRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (share.isRevoked() || !share.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "This shared report has expired or been revoked");
        }
        try {
            HealthcareReportController.HealthcareReport report = objectMapper.readValue(share.getReportSnapshot(),
                    HealthcareReportController.HealthcareReport.class);
            share.recordAccess();
            shareRepository.save(share);
            return report;
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Shared report could not be read");
        }
    }

    private ShareSummary summary(ReportShare share) {
        String status = share.isRevoked() ? "REVOKED"
                : share.getExpiresAt().isAfter(OffsetDateTime.now()) ? "ACTIVE" : "EXPIRED";
        return new ShareSummary(share.getId(), share.getCreatedAt(), share.getExpiresAt(),
                share.isJournalIncluded(), status, share.getAccessCount(), share.getLastAccessedAt());
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private String token() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record CreatedShare(UUID id, String token, OffsetDateTime expiresAt, boolean journalIncluded) {}
    public record ShareSummary(UUID id, OffsetDateTime createdAt, OffsetDateTime expiresAt,
            boolean journalIncluded, String status, long accessCount, OffsetDateTime lastAccessedAt) {}
}