package com.maatricare.tracking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDetailRepository extends JpaRepository<TaskDetail, UUID> {
    Optional<TaskDetail> findByTitleIgnoreCase(String title);
    List<TaskDetail> findAllByOrderByTitleAsc();
}