package com.maatricare.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportShareRepository extends JpaRepository<ReportShare, UUID> {
    Optional<ReportShare> findByTokenHash(String tokenHash);
    Optional<ReportShare> findByIdAndUserId(UUID id, UUID userId);
    List<ReportShare> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByUserId(UUID userId);
}