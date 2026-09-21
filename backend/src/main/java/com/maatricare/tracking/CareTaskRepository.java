package com.maatricare.tracking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareTaskRepository extends JpaRepository<CareTask, UUID> {
    @Override
    @EntityGraph(attributePaths = { "taskDetail", "user" })
    Optional<CareTask> findById(UUID id);

    @EntityGraph(attributePaths = "taskDetail")
    List<CareTask> findByUserIdAndTaskDateOrderByIdAsc(UUID userId, LocalDate taskDate);

    @EntityGraph(attributePaths = "taskDetail")
    List<CareTask> findByUserIdAndTaskDateBetweenOrderByTaskDateAscIdAsc(UUID userId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = "taskDetail")
    Optional<CareTask> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndTaskDateAndTaskDetailId(UUID userId, LocalDate taskDate, UUID taskDetailId);
    void deleteByUserId(UUID userId);
}