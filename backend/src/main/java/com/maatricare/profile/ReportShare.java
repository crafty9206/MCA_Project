package com.maatricare.profile;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.maatricare.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_shares")
public class ReportShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Lob
    @Column(name = "report_snapshot", nullable = false, columnDefinition = "TEXT")
    private String reportSnapshot;

    @Column(name = "journal_included", nullable = false)
    private boolean journalIncluded;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "access_count", nullable = false)
    private long accessCount;

    @Column(name = "last_accessed_at")
    private OffsetDateTime lastAccessedAt;

    protected ReportShare() {}

    public ReportShare(User user, String tokenHash, String reportSnapshot, boolean journalIncluded,
            OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.reportSnapshot = reportSnapshot;
        this.journalIncluded = journalIncluded;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public String getReportSnapshot() { return reportSnapshot; }
    public boolean isJournalIncluded() { return journalIncluded; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public long getAccessCount() { return accessCount; }
    public OffsetDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void revoke() { revoked = true; }
    public void recordAccess() { accessCount += 1; lastAccessedAt = OffsetDateTime.now(); }
}