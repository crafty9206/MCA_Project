package com.maatricare.notifications;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushReminderRepository extends JpaRepository<PushReminder, UUID> {
    Optional<PushReminder> findByUserIdAndDeduplicationKey(UUID userId, String deduplicationKey);
    Optional<PushReminder> findByDeduplicationKeyAndSnoozeTokenHash(String deduplicationKey, String snoozeTokenHash);
    List<PushReminder> findByUserIdAndStatusIn(UUID userId, List<String> statuses);

    @EntityGraph(attributePaths = "user")
    List<PushReminder> findTop50ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(String status, Instant now);

    @Modifying
    @Query("update PushReminder reminder set reminder.status = 'SENDING', reminder.claimedAt = :now "
            + "where reminder.id = :id and reminder.status = 'PENDING' and reminder.scheduledAt <= :now")
    int claim(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying
    @Query("update PushReminder reminder set reminder.status = 'PENDING', reminder.claimedAt = null "
            + "where reminder.status = 'SENDING' and reminder.claimedAt < :leaseExpiry")
    int releaseExpiredClaims(@Param("leaseExpiry") Instant leaseExpiry);

        long deleteByStatusInAndDeliveredAtBefore(List<String> statuses, Instant expiry);
}