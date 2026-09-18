package com.maatricare.pregnancy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PregnancyProfileRepository extends JpaRepository<PregnancyProfile, UUID> {

    Optional<PregnancyProfile> findByUserId(UUID userId);
}