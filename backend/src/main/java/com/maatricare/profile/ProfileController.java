package com.maatricare.profile;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.pregnancy.PregnancyProfile;
import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;

    public ProfileController(UserRepository userRepository, PregnancyProfileRepository pregnancyProfileRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        User user = currentUser(authentication);
        return toResponse(user, pregnancyProfileRepository.findByUserId(user.getId()).orElse(null));
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
        return toResponse(user, profile);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }

    private ProfileResponse toResponse(User user, PregnancyProfile profile) {
        PregnancySummary pregnancy = profile == null ? null : pregnancySummary(profile);
        return new ProfileResponse(user.getEmail(), user.getDisplayName(), user.getPreferredLanguage(), pregnancy);
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

    public record ProfileResponse(String email, String displayName, String preferredLanguage,
            PregnancySummary pregnancy) {
    }

    public record PregnancySummary(LocalDate lastMenstrualPeriod, LocalDate dueDate, int currentWeek,
            String trimester, int weeksRemaining, Integer ageYears, BigDecimal heightCm, BigDecimal prePregnancyWeightKg,
            String bloodPressure, String bloodGroup) {
    }
}
