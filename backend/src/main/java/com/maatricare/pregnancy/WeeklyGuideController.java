package com.maatricare.pregnancy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/weekly-guide")
public class WeeklyGuideController {

    private final UserRepository userRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;

    public WeeklyGuideController(UserRepository userRepository,
            PregnancyProfileRepository pregnancyProfileRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
    }

    @GetMapping
    public WeeklyGuideResponse currentGuide(Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        PregnancyProfile profile = pregnancyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Create a pregnancy profile to view the weekly guide"));
        int week = currentWeek(profile.getLastMenstrualPeriod());
        return guideFor(week);
    }

    private int currentWeek(LocalDate lastMenstrualPeriod) {
        long daysSinceLmp = ChronoUnit.DAYS.between(lastMenstrualPeriod, LocalDate.now());
        return (int) Math.max(0, Math.min(40, daysSinceLmp / 7));
    }

    private WeeklyGuideResponse guideFor(int week) {
        if (week < 14) {
            return new WeeklyGuideResponse(week, "First trimester", "Early pregnancy care",
                "Your body is adjusting to pregnancy, and regular nourishment and rest can support your daily routine.",
                List.of("Try regular, balanced meals and sip fluids throughout the day.",
                        "Discuss prenatal vitamins and supplements with your healthcare professional."),
                List.of("Choose gentle movement only if your healthcare professional says it is appropriate.",
                        "Pause and seek advice if activity causes concerning symptoms."),
                List.of("Changes in energy, appetite, and nausea are commonly discussed in early pregnancy."),
                List.of("Which symptoms should prompt me to contact you?", "Are my current medicines and supplements appropriate?"),
                "1.0", LocalDate.of(2026, 9, 26));
        }
        if (week < 28) {
            return new WeeklyGuideResponse(week, "Second trimester", "Building a steady routine",
                "Many people use the middle weeks to establish sustainable routines for meals, movement, sleep, and appointments.",
                List.of("Include a variety of foods and continue regular hydration.",
                        "Ask your healthcare professional about nutrition needs specific to you."),
                List.of("Walking or gentle stretching may be suitable when approved by your care team.",
                        "Build activity gradually and include rest."),
                List.of("Body shape, sleep, energy, and movement awareness may change during these weeks."),
                List.of("What changes should I expect before my next appointment?", "What activity level is suitable for me?"),
                "1.0", LocalDate.of(2026, 9, 26));
        }
        return new WeeklyGuideResponse(week, "Third trimester", "Preparing for the final stretch",
            "Later pregnancy can be a useful time to review practical support, birth preparation, and when to contact your care team.",
            List.of("Continue balanced meals and hydration in ways that feel manageable.",
                    "Discuss any nutrition or supplement concerns with your healthcare professional."),
            List.of("Use comfortable, approved movement and allow additional time for rest.",
                    "Ask your care team when activity should be reduced or stopped."),
            List.of("Comfort, sleep, energy, and movement patterns may continue to change."),
            List.of("When should I contact the maternity unit?", "What should I prepare for birth and the hospital?"),
            "1.0", LocalDate.of(2026, 9, 26));
    }

    public record WeeklyGuideResponse(int week, String trimester, String title, String summary,
            List<String> nutrition, List<String> activity, List<String> bodyChanges,
            List<String> questionsForClinician, String contentVersion, LocalDate reviewedOn) {}
}