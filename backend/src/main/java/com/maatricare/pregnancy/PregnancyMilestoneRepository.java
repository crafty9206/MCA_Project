package com.maatricare.pregnancy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PregnancyMilestoneRepository extends JpaRepository<PregnancyMilestone, UUID> {
    List<PregnancyMilestone> findByUserIdOrderByWeekNumberAsc(UUID userId);
    Optional<PregnancyMilestone> findByIdAndUserId(UUID id, UUID userId);
    void deleteByUserId(UUID userId);
}
