package com.maatricare.tracking;

import java.time.Instant;
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
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "provider_name", length = 160)
    private String providerName;

    @Column(name = "clinic_name", length = 200)
    private String clinicName;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Appointment() {
    }

    public Appointment(User user, String title, OffsetDateTime startsAt, OffsetDateTime endsAt,
            String providerName, String clinicName, String notes) {
        this.user = user;
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.providerName = providerName;
        this.clinicName = clinicName;
        this.notes = notes;
    }

    @jakarta.persistence.PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public String getProviderName() { return providerName; }
    public String getClinicName() { return clinicName; }
    public String getNotes() { return notes; }
}