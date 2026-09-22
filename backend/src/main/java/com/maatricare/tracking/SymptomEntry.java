package com.maatricare.tracking;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "symptom_entries")
public class SymptomEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 80)
    private String symptom;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(length = 1000)
    private String notes;

    protected SymptomEntry() {
    }

    public SymptomEntry(User user, String symptom, String severity, OffsetDateTime occurredAt, String notes) {
        this.user = user;
        this.symptom = symptom;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getSymptom() { return symptom; }
    public String getSeverity() { return severity; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public String getNotes() { return notes; }

    public void update(String symptom, String severity, OffsetDateTime occurredAt, String notes) {
        this.symptom = symptom;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.notes = notes;
    }
}