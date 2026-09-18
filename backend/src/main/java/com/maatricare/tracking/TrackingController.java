package com.maatricare.tracking;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api")
public class TrackingController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CareTaskRepository careTaskRepository;

    public TrackingController(UserRepository userRepository, AppointmentRepository appointmentRepository,
            CareTaskRepository careTaskRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> appointments(Authentication authentication) {
        User user = currentUser(authentication);
        return appointmentRepository.findByUserIdOrderByStartsAtAsc(user.getId()).stream().map(this::appointmentResponse).toList();
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(Authentication authentication, @Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentRepository.save(new Appointment(currentUser(authentication), request.title(),
                request.startsAt(), request.endsAt(), request.providerName(), request.clinicName(), request.notes()));
        return appointmentResponse(appointment);
    }

    @GetMapping("/tasks")
    public List<TaskResponse> tasks(Authentication authentication, @RequestParam(required = false) LocalDate date) {
        User user = currentUser(authentication);
        LocalDate taskDate = date == null ? LocalDate.now() : date;
        return careTaskRepository.findByUserIdAndTaskDateOrderByIdAsc(user.getId(), taskDate).stream().map(this::taskResponse).toList();
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(Authentication authentication, @Valid @RequestBody TaskRequest request) {
        CareTask task = careTaskRepository.save(new CareTask(currentUser(authentication), request.title(), request.taskDate()));
        return taskResponse(task);
    }

    @PatchMapping("/tasks/{id}/complete")
    public TaskResponse completeTask(Authentication authentication, @PathVariable UUID id, @RequestBody CompletionRequest request) {
        CareTask task = careTaskRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Care task not found"));
        task.setCompleted(request.completed());
        return taskResponse(careTaskRepository.save(task));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
    }

    private AppointmentResponse appointmentResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getTitle(), appointment.getStartsAt(), appointment.getEndsAt(),
                appointment.getProviderName(), appointment.getClinicName(), appointment.getNotes());
    }

    private TaskResponse taskResponse(CareTask task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getTaskDate(), task.isCompleted());
    }

    public record AppointmentRequest(@NotBlank @Size(max = 160) String title, @NotNull OffsetDateTime startsAt,
            OffsetDateTime endsAt, @Size(max = 160) String providerName, @Size(max = 200) String clinicName,
            @Size(max = 1000) String notes) {}
    public record AppointmentResponse(UUID id, String title, OffsetDateTime startsAt, OffsetDateTime endsAt,
            String providerName, String clinicName, String notes) {}
    public record TaskRequest(@NotBlank @Size(max = 160) String title, @NotNull LocalDate taskDate) {}
    public record CompletionRequest(boolean completed) {}
    public record TaskResponse(UUID id, String title, LocalDate taskDate, boolean completed) {}
}