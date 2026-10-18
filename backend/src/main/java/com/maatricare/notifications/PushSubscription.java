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
@Table(name = "push_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "endpoint_hash" }))
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 2048)
    private String endpoint;

    @Column(name = "endpoint_hash", nullable = false, length = 64)
    private String endpointHash;

    @Column(name = "p256dh_key", nullable = false, length = 255)
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, length = 255)
    private String authKey;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PushSubscription() {
    }

    public PushSubscription(User user, String endpoint, String endpointHash, String p256dhKey, String authKey) {
        this.user = user;
        update(endpoint, endpointHash, p256dhKey, authKey);
    }

    public void update(String endpoint, String endpointHash, String p256dhKey, String authKey) {
        this.endpoint = endpoint;
        this.endpointHash = endpointHash;
        this.p256dhKey = p256dhKey;
        this.authKey = authKey;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getEndpoint() { return endpoint; }
    public String getP256dhKey() { return p256dhKey; }
    public String getAuthKey() { return authKey; }
}