package com.maatricare.tracking;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyWellbeingRepository extends JpaRepository<DailyWellbeing, UUID> {
    Optional<DailyWellbeing> findByUserIdAndEntryDate(UUID userId, LocalDate entryDate);
    void deleteByUserId(UUID userId);
}