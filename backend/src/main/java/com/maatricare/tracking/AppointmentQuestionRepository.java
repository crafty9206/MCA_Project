package com.maatricare.tracking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentQuestionRepository extends JpaRepository<AppointmentQuestion, UUID> {
    List<AppointmentQuestion> findByAppointment_IdAndAppointment_User_IdOrderByIdAsc(UUID appointmentId, UUID userId);
    Optional<AppointmentQuestion> findByIdAndAppointment_User_Id(UUID id, UUID userId);
}