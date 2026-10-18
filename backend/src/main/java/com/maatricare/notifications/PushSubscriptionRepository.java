package com.maatricare.notifications;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {
    Optional<PushSubscription> findByUserIdAndEndpointHash(UUID userId, String endpointHash);
    List<PushSubscription> findByUserId(UUID userId);
    long countByUserId(UUID userId);
    void deleteByUserId(UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);
}