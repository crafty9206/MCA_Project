package com.maatricare.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
    List<EmergencyContact> findByUserIdOrderByNameAsc(UUID userId);
    Optional<EmergencyContact> findByIdAndUserId(UUID id, UUID userId);
    void deleteByUserId(UUID userId);
}