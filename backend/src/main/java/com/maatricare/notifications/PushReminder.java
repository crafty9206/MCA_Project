package com.maatricare.notifications;

import java.time.Instant;
import java.util.UUID;

import com.maatricare.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "push_reminders", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "deduplication_key" }))
public class PushReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deduplication_key", nullable = false, length = 300)
    private String deduplicationKey;

    @Column(name = "reminder_type", nullable = false, length = 20)
    private String reminderType;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "snoozed_until")
    private Instant snoozedUntil;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(nullable = false, length = 16)
    private String status = "PENDING";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "snooze_token_hash", length = 64)
    private String snoozeTokenHash;

    @Column(name = "snooze_token_expires_at")
    private Instant snoozeTokenExpiresAt;

    protected PushReminder() {
    }

    public PushReminder(User user, String deduplicationKey, String reminderType, String title, String body,
            Instant scheduledAt) {
        this.user = user;
        this.deduplicationKey = deduplicationKey;
        this.reminderType = reminderType;
        this.title = title;
        this.body = body;
        this.scheduledAt = scheduledAt;
    }

    public void updateContent(String reminderType, String title, String body) {
        this.reminderType = reminderType;
        this.title = title;
        this.body = body;
    }

    public void refresh(String reminderType, String title, String body, Instant scheduledAt) {
        this.reminderType = reminderType;
        this.title = title;
        this.body = body;
        boolean retryBackoffActive = "PENDING".equals(status) && attemptCount > 0
                && this.scheduledAt.isAfter(Instant.now());
        if (snoozedUntil == null || !snoozedUntil.isAfter(Instant.now())) {
            if (!retryBackoffActive) this.scheduledAt = scheduledAt;
            this.snoozedUntil = null;
        }
        if ("CANCELLED".equals(status)) {
            this.status = "PENDING";
            this.deliveredAt = null;
            this.attemptCount = 0;
        }
    }

    public void snoozeUntil(Instant until) {
        this.snoozedUntil = until;
        this.scheduledAt = until;
        this.deliveredAt = null;
        this.status = "PENDING";
        this.claimedAt = null;
        this.snoozeTokenHash = null;
        this.snoozeTokenExpiresAt = null;
    }

    public void markClaimed(Instant now, String tokenHash, Instant tokenExpiresAt) { this.status = "SENDING"; this.claimedAt = now; this.snoozeTokenHash = tokenHash; this.snoozeTokenExpiresAt = tokenExpiresAt; }
    public void markDelivered(Instant now) { this.status = "SENT"; this.deliveredAt = now; this.claimedAt = null; }
    public void retryAt(Instant nextAttempt) { this.status = "PENDING"; this.scheduledAt = nextAttempt; this.attemptCount++; this.claimedAt = null; this.snoozeTokenHash = null; this.snoozeTokenExpiresAt = null; }
    public void cancel(Instant now) { this.status = "CANCELLED"; this.deliveredAt = now; this.claimedAt = null; }
    public void markFailed(Instant now) { this.status = "FAILED"; this.deliveredAt = now; this.claimedAt = null; }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getDeduplicationKey() { return deduplicationKey; }
    public String getReminderType() { return reminderType; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getSnoozedUntil() { return snoozedUntil; }
    public String getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public String getSnoozeTokenHash() { return snoozeTokenHash; }
    public Instant getSnoozeTokenExpiresAt() { return snoozeTokenExpiresAt; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
}