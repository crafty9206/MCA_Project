package com.maatricare.tracking;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/wellbeing")
public class WellbeingController {

    private final UserRepository userRepository;
    private final DailyWellbeingRepository wellbeingRepository;
    private final CareTaskRepository careTaskRepository;

    public WellbeingController(UserRepository userRepository, DailyWellbeingRepository wellbeingRepository,
            CareTaskRepository careTaskRepository) {
        this.userRepository = userRepository;
        this.wellbeingRepository = wellbeingRepository;
        this.careTaskRepository = careTaskRepository;
    }

    @GetMapping
    public WellbeingResponse wellbeing(Authentication authentication, @RequestParam(required = false) LocalDate date) {
        User user = currentUser(authentication);
        LocalDate entryDate = date == null ? LocalDate.now() : date;
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), entryDate)
                .orElseGet(() -> wellbeingRepository.save(new DailyWellbeing(user, entryDate)));
        return response(wellbeing);
    }

    @PatchMapping("/water")
    public WellbeingResponse updateWater(Authentication authentication, @Valid @RequestBody WaterRequest request) {
        User user = currentUser(authentication);
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), request.date())
                .orElseGet(() -> new DailyWellbeing(user, request.date()));
        wellbeing.setWaterGlasses(request.glasses());
        return response(wellbeingRepository.save(wellbeing));
    }

    @PatchMapping("/prenatal-vitamin")
    public WellbeingResponse updatePrenatalVitamin(Authentication authentication,
            @Valid @RequestBody VitaminRequest request) {
        User user = currentUser(authentication);
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), request.date())
                .orElseGet(() -> new DailyWellbeing(user, request.date()));
        wellbeing.setPrenatalVitaminTaken(request.taken());
        return response(wellbeingRepository.save(wellbeing));
    }

    @PatchMapping("/activity")
    public WellbeingResponse updateActivity(Authentication authentication,
            @Valid @RequestBody ActivityRequest request) {
        User user = currentUser(authentication);
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), request.date())
                .orElseGet(() -> new DailyWellbeing(user, request.date()));
        wellbeing.setActivityMinutes(request.minutes());
        return response(wellbeingRepository.save(wellbeing));
    }

    @PatchMapping("/mood")
    public WellbeingResponse updateMood(Authentication authentication,
            @Valid @RequestBody MoodRequest request) {
        User user = currentUser(authentication);
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), request.date())
                .orElseGet(() -> new DailyWellbeing(user, request.date()));
        wellbeing.setMood(request.mood());
        return response(wellbeingRepository.save(wellbeing));
    }

    @PatchMapping("/sleep")
    public WellbeingResponse updateSleep(Authentication authentication,
            @Valid @RequestBody SleepRequest request) {
        User user = currentUser(authentication);
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), request.date())
                .orElseGet(() -> new DailyWellbeing(user, request.date()));
        wellbeing.setSleepHours(request.hours());
        return response(wellbeingRepository.save(wellbeing));
    }

    @PatchMapping("/day-plan")
    public WellbeingResponse updateDayPlan(Authentication authentication,
            @Valid @RequestBody DayPlanRequest request) {
        String energyLevel = request.energyLevel().trim().toUpperCase();
        if (!List.of("LOW", "STEADY", "HIGH").contains(energyLevel)) {
            throw new IllegalArgumentException("Energy level must be LOW, STEADY, or HIGH");
        }
        User user = currentUser(authentication);
        if (request.focusTaskIds().size() != request.focusTaskCount()
                || request.focusTaskIds().stream().distinct().count() != request.focusTaskIds().size()) {
            throw new IllegalArgumentException("Choose exactly the requested number of unique focus tasks");
        }
        request.focusTaskIds().forEach(taskId -> {
            CareTask task = careTaskRepository.findByIdAndUserId(taskId, user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Every focus task must belong to the current user"));
            if (!task.getTaskDate().equals(request.date()) || task.isCompleted()) {
                throw new IllegalArgumentException("Focus tasks must be unfinished tasks from the selected date");
            }
        });
        DailyWellbeing wellbeing = wellbeingRepository.findByUserIdAndEntryDate(user.getId(), request.date())
                .orElseGet(() -> new DailyWellbeing(user, request.date()));
        wellbeing.updateDayPlan(energyLevel, request.focusTaskCount(), request.focusTaskIds());
        return response(wellbeingRepository.save(wellbeing));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
    }

    private WellbeingResponse response(DailyWellbeing wellbeing) {
        return new WellbeingResponse(wellbeing.getEntryDate(), wellbeing.getWaterGlasses(), 8,
            wellbeing.isPrenatalVitaminTaken(), wellbeing.getActivityMinutes(), 20, wellbeing.getMood(),
            wellbeing.getSleepHours(), new BigDecimal("8.0"), wellbeing.getEnergyLevel(),
            wellbeing.getFocusTaskCount(), wellbeing.getFocusTaskIds().stream().toList());
    }

    public record WaterRequest(LocalDate date, @Min(0) @Max(30) int glasses) {}
    public record VitaminRequest(LocalDate date, boolean taken) {}
    public record ActivityRequest(LocalDate date, @Min(0) @Max(300) int minutes) {}
    public record MoodRequest(LocalDate date, @Size(max = 40) String mood) {}
        public record SleepRequest(LocalDate date, @Min(0) @Max(24) BigDecimal hours) {}
    public record DayPlanRequest(@NotNull LocalDate date, @NotBlank String energyLevel,
            @Min(1) @Max(5) int focusTaskCount, @NotNull @Size(min = 1, max = 5) List<UUID> focusTaskIds) {}
    public record WellbeingResponse(LocalDate date, int waterGlasses, int waterGoal,
            boolean prenatalVitaminTaken, int activityMinutes, int activityGoal, String mood,
            BigDecimal sleepHours, BigDecimal sleepGoal, String energyLevel, int focusTaskCount,
            List<UUID> focusTaskIds) {}
}