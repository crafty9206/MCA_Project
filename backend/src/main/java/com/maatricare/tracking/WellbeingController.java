package com.maatricare.tracking;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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

    public WellbeingController(UserRepository userRepository, DailyWellbeingRepository wellbeingRepository) {
        this.userRepository = userRepository;
        this.wellbeingRepository = wellbeingRepository;
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

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
    }

    private WellbeingResponse response(DailyWellbeing wellbeing) {
        return new WellbeingResponse(wellbeing.getEntryDate(), wellbeing.getWaterGlasses(), 8,
            wellbeing.isPrenatalVitaminTaken(), wellbeing.getActivityMinutes(), 20);
    }

    public record WaterRequest(LocalDate date, @Min(0) @Max(30) int glasses) {}
    public record VitaminRequest(LocalDate date, boolean taken) {}
        public record ActivityRequest(LocalDate date, @Min(0) @Max(300) int minutes) {}
    public record WellbeingResponse(LocalDate date, int waterGlasses, int waterGoal,
            boolean prenatalVitaminTaken, int activityMinutes, int activityGoal) {}
}