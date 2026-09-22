package com.maatricare.tracking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SymptomEntryRepository extends JpaRepository<SymptomEntry, UUID> {
    List<SymptomEntry> findByUserIdOrderByOccurredAtDesc(UUID userId);
    Optional<SymptomEntry> findByIdAndUserId(UUID id, UUID userId);
    void deleteByUserId(UUID userId);
}