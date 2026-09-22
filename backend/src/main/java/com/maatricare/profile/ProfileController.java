package com.maatricare.profile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.pregnancy.PregnancyMilestone;
import com.maatricare.pregnancy.PregnancyMilestoneRepository;
import com.maatricare.pregnancy.PregnancyProfile;
import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.tracking.CareTask;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.tracking.TaskDetailRepository;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final PregnancyMilestoneRepository pregnancyMilestoneRepository;
    private final CareTaskRepository careTaskRepository;
    private final TaskDetailRepository taskDetailRepository;

    public ProfileController(UserRepository userRepository, PregnancyProfileRepository pregnancyProfileRepository,
            PregnancyMilestoneRepository pregnancyMilestoneRepository, CareTaskRepository careTaskRepository,
            TaskDetailRepository taskDetailRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.pregnancyMilestoneRepository = pregnancyMilestoneRepository;
        this.careTaskRepository = careTaskRepository;
        this.taskDetailRepository = taskDetailRepository;
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        User user = currentUser(authentication);
        ensureDefaultTasks(user, LocalDate.now());
        return toResponse(user, pregnancyProfileRepository.findByUserId(user.getId()).orElse(null));
    }

    @PatchMapping
    public ProfileResponse updateProfile(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        User user = currentUser(authentication);
        user.setDisplayName(request.displayName().trim());
        user.updateNotificationPreferences(
            request.appointmentRemindersEnabled() == null ? user.isAppointmentRemindersEnabled() : request.appointmentRemindersEnabled(),
            request.browserNotificationsEnabled() == null ? user.isBrowserNotificationsEnabled() : request.browserNotificationsEnabled(),
            request.dailyCareRemindersEnabled() == null ? user.isDailyCareRemindersEnabled() : request.dailyCareRemindersEnabled(),
                request.dailyCareReminderTime() == null ? user.getDailyCareReminderTime() : request.dailyCareReminderTime(),
                request.weeklyPregnancyRemindersEnabled() == null ? user.isWeeklyPregnancyRemindersEnabled() : request.weeklyPregnancyRemindersEnabled(),
                request.weeklyPregnancyReminderDay() == null ? user.getWeeklyPregnancyReminderDay() : request.weeklyPregnancyReminderDay(),
                request.weeklyPregnancyReminderTime() == null ? user.getWeeklyPregnancyReminderTime() : request.weeklyPregnancyReminderTime(),
                request.missedTaskRemindersEnabled() == null ? user.isMissedTaskRemindersEnabled() : request.missedTaskRemindersEnabled());
        User savedUser = userRepository.save(user);
        return toResponse(savedUser, pregnancyProfileRepository.findByUserId(savedUser.getId()).orElse(null));
    }

    @PostMapping("/pregnancy")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createPregnancyProfile(Authentication authentication,
            @Valid @RequestBody PregnancyRequest request) {
        User user = currentUser(authentication);
        if (pregnancyProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalStateException("A pregnancy profile already exists");
        }
        LocalDate dueDate = request.lastMenstrualPeriod().plusDays(280);
        PregnancyProfile profile = pregnancyProfileRepository.save(
                new PregnancyProfile(user, request.lastMenstrualPeriod(), dueDate, request.ageYears(), request.heightCm(),
                        request.prePregnancyWeightKg(), request.bloodPressure(), request.bloodGroup()));
        createStandardMilestones(user);
        return toResponse(user, profile);
    }

    @PatchMapping("/pregnancy")
    public ProfileResponse updatePregnancyProfile(Authentication authentication,
            @Valid @RequestBody PregnancyRequest request) {
        User user = currentUser(authentication);
        PregnancyProfile profile = pregnancyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("A pregnancy profile does not exist"));
        profile.updateDetails(request.lastMenstrualPeriod(), request.lastMenstrualPeriod().plusDays(280),
                request.ageYears(), request.heightCm(), request.prePregnancyWeightKg(), request.bloodPressure(),
                request.bloodGroup());
        return toResponse(user, pregnancyProfileRepository.save(profile));
    }

    private void createStandardMilestones(User user) {
        var milestones = java.util.List.of(
                new PregnancyMilestone(user, "Confirm pregnancy", "Book the first prenatal appointment and review early pregnancy care guidance.", 4, "STANDARD"),
                new PregnancyMilestone(user, "First prenatal visit", "Meet with your healthcare professional to review health history and next steps.", 8, "STANDARD"),
                new PregnancyMilestone(user, "First trimester screening", "Review early screening and bloodwork recommendations with your clinician.", 12, "STANDARD"),
                new PregnancyMilestone(user, "Second trimester check-in", "Discuss movement, nutrition, and how you are feeling in the second trimester.", 16, "STANDARD"),
                new PregnancyMilestone(user, "Anatomy scan", "Prepare for the anatomy scan and review the baby's growth and development.", 20, "STANDARD"),
                new PregnancyMilestone(user, "Glucose screening", "Complete routine glucose screening during the second trimester.", 24, "STANDARD"),
                new PregnancyMilestone(user, "Third trimester planning", "Review birth preparation, circulation, and follow-up appointments.", 28, "STANDARD"),
                new PregnancyMilestone(user, "Prepare for birth", "Review hospital plans, transportation, and final checklist items.", 32, "STANDARD"),
                new PregnancyMilestone(user, "Final prenatal appointments", "Follow up on birth plans, symptoms, and final check-ins with your care team.", 36, "STANDARD"),
                new PregnancyMilestone(user, "Due date week", "Focus on rest, birth planning, and reaching your due date safely.", 40, "STANDARD")
        );
        milestones.forEach(pregnancyMilestoneRepository::save);
    }

    private void ensureDefaultTasks(User user, LocalDate taskDate) {
        java.util.List.of(
                "Take prenatal vitamins",
                "Drink 8 glasses of water",
                "Take a 20 minute walk",
                "Get enough rest"
        ).forEach(title -> {
            var taskDetail = taskDetailRepository.findByTitleIgnoreCase(title)
                    .orElseGet(() -> taskDetailRepository.save(new com.maatricare.tracking.TaskDetail(title, true)));
            if (!careTaskRepository.existsByUserIdAndTaskDateAndTaskDetailId(user.getId(), taskDate, taskDetail.getId())) {
                careTaskRepository.save(new CareTask(user, taskDetail, taskDate));
            }
        });
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }

    private ProfileResponse toResponse(User user, PregnancyProfile profile) {
        PregnancySummary pregnancy = profile == null ? null : pregnancySummary(profile);
        return new ProfileResponse(user.getEmail(), user.getDisplayName(), user.getPreferredLanguage(),
            user.isAppointmentRemindersEnabled(), user.isBrowserNotificationsEnabled(),
                user.isDailyCareRemindersEnabled(), user.getDailyCareReminderTime(),
                user.isWeeklyPregnancyRemindersEnabled(), user.getWeeklyPregnancyReminderDay(),
                user.getWeeklyPregnancyReminderTime(), user.isMissedTaskRemindersEnabled(), pregnancy);
    }

    private PregnancySummary pregnancySummary(PregnancyProfile profile) {
        long daysSinceLmp = ChronoUnit.DAYS.between(profile.getLastMenstrualPeriod(), LocalDate.now());
        int week = (int) Math.max(0, Math.min(40, daysSinceLmp / 7));
        String trimester = week < 14 ? "First trimester" : week < 28 ? "Second trimester" : "Third trimester";
        return new PregnancySummary(profile.getLastMenstrualPeriod(), profile.getDueDate(), week, trimester,
                Math.max(0, 40 - week), profile.getAgeYears(), profile.getHeightCm(), profile.getPrePregnancyWeightKg(),
                profile.getBloodPressure(), profile.getBloodGroup());
    }

    public record PregnancyRequest(@NotNull LocalDate lastMenstrualPeriod,
            @NotNull @Min(13) @Max(60) Integer ageYears,
            @Min(50) @Max(250) BigDecimal heightCm,
            @Min(20) @Max(300) BigDecimal prePregnancyWeightKg,
            @Size(max = 20) String bloodPressure,
            @Size(max = 3) String bloodGroup) {
    }

        public record ProfileUpdateRequest(@NotNull @Size(min = 1, max = 120) String displayName,
                Boolean appointmentRemindersEnabled, Boolean browserNotificationsEnabled,
                Boolean dailyCareRemindersEnabled, LocalTime dailyCareReminderTime,
                Boolean weeklyPregnancyRemindersEnabled, @Min(1) @Max(7) Integer weeklyPregnancyReminderDay,
                LocalTime weeklyPregnancyReminderTime, Boolean missedTaskRemindersEnabled) {
    }

        public record ProfileResponse(String email, String displayName, String preferredLanguage,
                boolean appointmentRemindersEnabled, boolean browserNotificationsEnabled,
                boolean dailyCareRemindersEnabled, LocalTime dailyCareReminderTime,
                boolean weeklyPregnancyRemindersEnabled, int weeklyPregnancyReminderDay,
                LocalTime weeklyPregnancyReminderTime, boolean missedTaskRemindersEnabled,
                PregnancySummary pregnancy) {
    }

    public record PregnancySummary(LocalDate lastMenstrualPeriod, LocalDate dueDate, int currentWeek,
            String trimester, int weeksRemaining, Integer ageYears, BigDecimal heightCm, BigDecimal prePregnancyWeightKg,
            String bloodPressure, String bloodGroup) {
    }
}
