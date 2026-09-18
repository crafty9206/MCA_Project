package com.maatricare.tracking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CareTaskRepository extends JpaRepository<CareTask, UUID> {
    List<CareTask> findByUserIdAndTaskDateOrderByIdAsc(UUID userId, LocalDate taskDate);
    Optional<CareTask> findByIdAndUserId(UUID id, UUID userId);
}