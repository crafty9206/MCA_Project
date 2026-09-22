package com.maatricare.tracking;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final TaskDetailRepository taskDetailRepository;
        private final AppointmentQuestionRepository appointmentQuestionRepository;
        private final SymptomEntryRepository symptomEntryRepository;

    public TrackingController(UserRepository userRepository, AppointmentRepository appointmentRepository,
            CareTaskRepository careTaskRepository, TaskDetailRepository taskDetailRepository,
            AppointmentQuestionRepository appointmentQuestionRepository, SymptomEntryRepository symptomEntryRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.taskDetailRepository = taskDetailRepository;
        this.appointmentQuestionRepository = appointmentQuestionRepository;
        this.symptomEntryRepository = symptomEntryRepository;
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
            request.startsAt(), request.endsAt(), request.providerName(), request.clinicName(), request.notes(),
            request.reminderMinutesBefore()));
        return appointmentResponse(appointment);
    }

    @PatchMapping("/appointments/{id}")
    public AppointmentResponse updateAppointment(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Appointment not found"));
        appointment.updateDetails(request.title(), request.startsAt(), request.endsAt(), request.providerName(),
            request.clinicName(), request.notes(), request.reminderMinutesBefore());
        return appointmentResponse(appointmentRepository.save(appointment));
    }

    @DeleteMapping("/appointments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAppointment(Authentication authentication, @PathVariable UUID id) {
        Appointment appointment = appointmentRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Appointment not found"));
        appointmentRepository.delete(appointment);
    }

        @GetMapping("/appointments/{appointmentId}/questions")
        public List<QuestionResponse> questions(Authentication authentication, @PathVariable UUID appointmentId) {
        User user = currentUser(authentication);
        appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
            .orElseThrow(() -> new java.util.NoSuchElementException("Appointment not found"));
        return appointmentQuestionRepository
            .findByAppointment_IdAndAppointment_User_IdOrderByIdAsc(appointmentId, user.getId()).stream()
            .map(this::questionResponse).toList();
        }

        @PostMapping("/appointments/{appointmentId}/questions")
        @ResponseStatus(HttpStatus.CREATED)
        public QuestionResponse createQuestion(Authentication authentication, @PathVariable UUID appointmentId,
            @Valid @RequestBody QuestionRequest request) {
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, currentUser(authentication).getId())
            .orElseThrow(() -> new java.util.NoSuchElementException("Appointment not found"));
        return questionResponse(appointmentQuestionRepository.save(
            new AppointmentQuestion(appointment, request.question().trim())));
        }

        @PatchMapping("/appointment-questions/{id}")
        public QuestionResponse updateQuestion(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody QuestionUpdateRequest request) {
        AppointmentQuestion question = appointmentQuestionRepository
            .findByIdAndAppointment_User_Id(id, currentUser(authentication).getId())
            .orElseThrow(() -> new java.util.NoSuchElementException("Appointment question not found"));
        question.update(request.question().trim(), request.answered());
        return questionResponse(appointmentQuestionRepository.save(question));
        }

        @DeleteMapping("/appointment-questions/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteQuestion(Authentication authentication, @PathVariable UUID id) {
        AppointmentQuestion question = appointmentQuestionRepository
            .findByIdAndAppointment_User_Id(id, currentUser(authentication).getId())
            .orElseThrow(() -> new java.util.NoSuchElementException("Appointment question not found"));
        appointmentQuestionRepository.delete(question);
        }

    @GetMapping("/symptoms")
    public List<SymptomResponse> symptoms(Authentication authentication) {
        return symptomEntryRepository.findByUserIdOrderByOccurredAtDesc(currentUser(authentication).getId()).stream()
                .map(this::symptomResponse).toList();
    }

    @PostMapping("/symptoms")
    @ResponseStatus(HttpStatus.CREATED)
    public SymptomResponse createSymptom(Authentication authentication,
            @Valid @RequestBody SymptomRequest request) {
        validateSeverity(request.severity());
        SymptomEntry entry = symptomEntryRepository.save(new SymptomEntry(currentUser(authentication),
                request.symptom().trim(), request.severity().toUpperCase(), request.occurredAt(), request.notes()));
        return symptomResponse(entry);
    }

    @PatchMapping("/symptoms/{id}")
    public SymptomResponse updateSymptom(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody SymptomRequest request) {
        validateSeverity(request.severity());
        SymptomEntry entry = symptomEntryRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Symptom entry not found"));
        entry.update(request.symptom().trim(), request.severity().toUpperCase(), request.occurredAt(), request.notes());
        return symptomResponse(symptomEntryRepository.save(entry));
    }

    @DeleteMapping("/symptoms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSymptom(Authentication authentication, @PathVariable UUID id) {
        SymptomEntry entry = symptomEntryRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Symptom entry not found"));
        symptomEntryRepository.delete(entry);
    }

    private void validateSeverity(String severity) {
        if (!List.of("MILD", "MODERATE", "SEVERE").contains(severity.toUpperCase())) {
            throw new IllegalArgumentException("Severity must be MILD, MODERATE, or SEVERE");
        }
    }

    private SymptomResponse symptomResponse(SymptomEntry entry) {
        return new SymptomResponse(entry.getId(), entry.getSymptom(), entry.getSeverity(), entry.getOccurredAt(), entry.getNotes());
    }

    @GetMapping("/tasks")
    public List<TaskResponse> tasks(Authentication authentication, @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) {
        User user = currentUser(authentication);
        if (from != null || to != null) {
            LocalDate start = from == null ? to : from;
            LocalDate end = to == null ? from : to;
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("The task history start date must not be after the end date");
            }
            return careTaskRepository.findByUserIdAndTaskDateBetweenOrderByTaskDateAscIdAsc(user.getId(), start, end)
                    .stream().map(this::taskResponse).toList();
        }
        LocalDate taskDate = date == null ? LocalDate.now() : date;
        assignSharedTasks(user, taskDate);
        return careTaskRepository.findByUserIdAndTaskDateOrderByIdAsc(user.getId(), taskDate).stream().map(this::taskResponse).toList();
    }

    private void assignSharedTasks(User user, LocalDate taskDate) {
        taskDetailRepository.findBySharedTrueOrderByTitleAsc().stream()
            .filter(taskDetail -> !careTaskRepository.existsByUserIdAndTaskDateAndTaskDetailId(
                user.getId(), taskDate, taskDetail.getId()))
            .map(taskDetail -> new CareTask(user, taskDetail, taskDate))
                .forEach(careTaskRepository::save);
            careTaskRepository.findByUserId(user.getId()).stream()
                .map(CareTask::getTaskDetail)
                .filter(detail -> !detail.isShared() && !"NONE".equals(detail.getRecurrence()))
                .filter(detail -> isDue(detail, taskDate))
                .filter(detail -> !careTaskRepository.existsByUserIdAndTaskDateAndTaskDetailId(user.getId(), taskDate, detail.getId()))
                .map(detail -> new CareTask(user, detail, taskDate))
                .forEach(careTaskRepository::save);
    }

            private boolean isDue(TaskDetail detail, LocalDate date) {
            if (detail.getRecurrenceStartDate() == null || date.isBefore(detail.getRecurrenceStartDate())) return false;
            return "DAILY".equals(detail.getRecurrence())
                || "WEEKLY".equals(detail.getRecurrence()) && date.getDayOfWeek() == detail.getRecurrenceStartDate().getDayOfWeek();
            }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(Authentication authentication, @Valid @RequestBody TaskRequest request) {
        String recurrence = request.recurrence() == null ? "NONE" : request.recurrence().toUpperCase();
        if (!List.of("NONE", "DAILY", "WEEKLY").contains(recurrence)) {
            throw new IllegalArgumentException("Recurrence must be NONE, DAILY, or WEEKLY");
        }
        TaskDetail taskDetail = taskDetailRepository.findByTitleIgnoreCase(request.title().trim())
            .orElseGet(() -> taskDetailRepository.save(new TaskDetail(request.title().trim(), false, recurrence, request.taskDate())));
        CareTask task = careTaskRepository.save(new CareTask(currentUser(authentication), taskDetail, request.taskDate()));
        return new TaskResponse(task.getId(), taskDetail.getId(), taskDetail.getTitle(), task.getTaskDate(),
            task.isCompleted(), taskDetail.isShared());
    }

    @PatchMapping("/tasks/{id}/complete")
    public TaskResponse completeTask(Authentication authentication, @PathVariable UUID id, @RequestBody CompletionRequest request) {
        CareTask task = careTaskRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Care task not found"));
        task.setCompleted(request.completed());
        careTaskRepository.save(task);
        return taskResponse(task);
    }

    @PatchMapping("/tasks/{id}")
    public TaskResponse updateTask(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest request) {
        CareTask task = careTaskRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Care task not found"));
        if (task.getTaskDetail().isShared()) {
            throw new IllegalStateException("Shared care tasks can only be changed by an administrator");
        }
        task.getTaskDetail().setTitle(request.title().trim());
        return taskResponse(careTaskRepository.save(task));
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(Authentication authentication, @PathVariable UUID id) {
        CareTask task = careTaskRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Care task not found"));
        if (task.getTaskDetail().isShared()) {
            throw new IllegalStateException("Shared care tasks can only be removed by an administrator");
        }
        careTaskRepository.delete(task);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
    }

    private AppointmentResponse appointmentResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getTitle(), appointment.getStartsAt(), appointment.getEndsAt(),
            appointment.getProviderName(), appointment.getClinicName(), appointment.getNotes(),
            appointment.getReminderMinutesBefore());
    }

    private TaskResponse taskResponse(CareTask task) {
        TaskDetail taskDetail = task.getTaskDetail();
        return new TaskResponse(task.getId(), taskDetail.getId(), taskDetail.getTitle(), task.getTaskDate(),
            task.isCompleted(), taskDetail.isShared());
    }

    private QuestionResponse questionResponse(AppointmentQuestion question) {
        return new QuestionResponse(question.getId(), question.getAppointmentId(), question.getQuestion(),
                question.isAnswered());
    }

    public record AppointmentRequest(@NotBlank @Size(max = 160) String title, @NotNull OffsetDateTime startsAt,
            OffsetDateTime endsAt, @Size(max = 160) String providerName, @Size(max = 200) String clinicName,
            @Size(max = 1000) String notes, @Min(0) @Max(10080) Integer reminderMinutesBefore) {}
    public record AppointmentResponse(UUID id, String title, OffsetDateTime startsAt, OffsetDateTime endsAt,
            String providerName, String clinicName, String notes, Integer reminderMinutesBefore) {}
    public record QuestionRequest(@NotBlank @Size(max = 500) String question) {}
    public record QuestionUpdateRequest(@NotBlank @Size(max = 500) String question, boolean answered) {}
    public record QuestionResponse(UUID id, UUID appointmentId, String question, boolean answered) {}
        public record SymptomRequest(@NotBlank @Size(max = 80) String symptom,
            @NotBlank String severity, @NotNull OffsetDateTime occurredAt, @Size(max = 1000) String notes) {}
        public record SymptomResponse(UUID id, String symptom, String severity, OffsetDateTime occurredAt, String notes) {}
        public record TaskRequest(@NotBlank @Size(max = 160) String title, @NotNull LocalDate taskDate,
            String recurrence) {}
    public record TaskUpdateRequest(@NotBlank @Size(max = 160) String title) {}
    public record CompletionRequest(boolean completed) {}
        public record TaskResponse(UUID id, UUID taskDetailId, String title, LocalDate taskDate, boolean completed,
            boolean shared) {}
}