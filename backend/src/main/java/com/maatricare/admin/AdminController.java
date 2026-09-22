package com.maatricare.admin;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.tracking.Appointment;
import com.maatricare.tracking.AppointmentRepository;
import com.maatricare.tracking.CareTask;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.tracking.TaskDetail;
import com.maatricare.tracking.TaskDetailRepository;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CareTaskRepository careTaskRepository;
    private final TaskDetailRepository taskDetailRepository;

    public AdminController(UserRepository userRepository, AppointmentRepository appointmentRepository,
            CareTaskRepository careTaskRepository, TaskDetailRepository taskDetailRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.taskDetailRepository = taskDetailRepository;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userRepository.findAllByOrderByEmailAsc().stream()
                .map(user -> new UserResponse(user.getEmail(), user.getDisplayName(), user.getRole()))
                .toList();
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody AdminTaskRequest request) {
        TaskDetail taskDetail = taskDetailRepository.findByTitleIgnoreCase(request.title().trim())
            .orElseGet(() -> taskDetailRepository.save(new TaskDetail(request.title().trim(), true)));
        List<TaskResponse> created = userRepository.findByRoleOrderByEmailAsc("USER").stream()
            .filter(user -> !careTaskRepository.existsByUserIdAndTaskDateAndTaskDetailId(user.getId(), request.taskDate(), taskDetail.getId()))
            .map(user -> careTaskRepository.save(new CareTask(user, taskDetail, request.taskDate())))
            .map(task -> new TaskResponse(task.getId(), taskDetail.getId(), task.getUser().getEmail(),
                    taskDetail.getTitle(), task.getTaskDate(), task.isCompleted(), taskDetail.isShared()))
            .toList();
        if (created.isEmpty()) {
            throw new IllegalStateException("This task already exists for every user on that date");
        }
        return created.get(0);
    }

    @PatchMapping("/tasks/{id}")
    public TaskResponse updateTask(@PathVariable UUID id, @Valid @RequestBody AdminTaskUpdateRequest request) {
        CareTask task = careTaskRepository.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Care task not found"));
        task.setCompleted(request.completed());
        TaskDetail taskDetail = task.getTaskDetail();
        careTaskRepository.save(task);
        return new TaskResponse(task.getId(), taskDetail.getId(), task.getUser().getEmail(), taskDetail.getTitle(),
            task.getTaskDate(), task.isCompleted(), taskDetail.isShared());
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID id) {
        if (!careTaskRepository.existsById(id)) {
            throw new java.util.NoSuchElementException("Care task not found");
        }
        careTaskRepository.deleteById(id);
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(@Valid @RequestBody AdminAppointmentRequest request) {
        User user = user(request.userEmail());
        Appointment appointment = new Appointment(user, request.title(), request.startsAt(), request.endsAt(),
            request.providerName(), request.clinicName(), request.notes(), request.reminderMinutesBefore());
        return appointmentResponse(appointmentRepository.save(appointment));
    }

    @PatchMapping("/appointments/{id}")
    public AppointmentResponse updateAppointment(@PathVariable UUID id, @Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Appointment not found"));
        appointment.updateDetails(request.title(), request.startsAt(), request.endsAt(), request.providerName(),
            request.clinicName(), request.notes(), request.reminderMinutesBefore());
        return appointmentResponse(appointmentRepository.save(appointment));
    }

    @DeleteMapping("/appointments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAppointment(@PathVariable UUID id) {
        if (!appointmentRepository.existsById(id)) {
            throw new java.util.NoSuchElementException("Appointment not found");
        }
        appointmentRepository.deleteById(id);
    }

    private User user(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));
    }

    private TaskResponse taskResponse(CareTask task) {
        TaskDetail taskDetail = task.getTaskDetail();
        return new TaskResponse(task.getId(), taskDetail.getId(), task.getUser().getEmail(), taskDetail.getTitle(),
            task.getTaskDate(), task.isCompleted(), taskDetail.isShared());
    }

    private AppointmentResponse appointmentResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getUser().getEmail(), appointment.getTitle(),
            appointment.getStartsAt(), appointment.getEndsAt(), appointment.getProviderName(), appointment.getClinicName(),
            appointment.getNotes(), appointment.getReminderMinutesBefore());
    }

    public record UserResponse(String email, String displayName, String role) {}
            public record TaskResponse(UUID id, UUID taskDetailId, String userEmail, String title, LocalDate taskDate,
                boolean completed, boolean shared) {}
    public record AdminTaskRequest(@NotBlank @Size(max = 160) String title, @NotNull LocalDate taskDate) {}
    public record AdminTaskUpdateRequest(boolean completed) {}
    public record AppointmentResponse(UUID id, String userEmail, String title, OffsetDateTime startsAt,
            OffsetDateTime endsAt, String providerName, String clinicName, String notes, Integer reminderMinutesBefore) {}
    public record AdminAppointmentRequest(@NotBlank @Size(max = 320) String userEmail, @NotBlank @Size(max = 160) String title,
            @NotNull OffsetDateTime startsAt, OffsetDateTime endsAt, @Size(max = 160) String providerName,
            @Size(max = 200) String clinicName, @Size(max = 1000) String notes, Integer reminderMinutesBefore) {}
    public record AppointmentRequest(@NotBlank @Size(max = 160) String title, @NotNull OffsetDateTime startsAt,
            OffsetDateTime endsAt, @Size(max = 160) String providerName, @Size(max = 200) String clinicName,
            @Size(max = 1000) String notes, Integer reminderMinutesBefore) {}
}
