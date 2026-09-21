package com.maatricare.tracking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByUserIdOrderByStartsAtAsc(UUID userId);
    Optional<Appointment> findByIdAndUserId(UUID id, UUID userId);
    void deleteByUserId(UUID userId);
}