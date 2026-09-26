package com.maatricare.profile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.maatricare.pregnancy.PregnancyMilestoneRepository;
import com.maatricare.pregnancy.PregnancyProfile;
import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.tracking.AppointmentRepository;
import com.maatricare.tracking.CareTask;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.tracking.DailyWellbeing;
import com.maatricare.tracking.DailyWellbeingRepository;
import com.maatricare.tracking.SymptomEntryRepository;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/reports")
public class HealthcareReportController {

    private final UserRepository userRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final CareTaskRepository careTaskRepository;
    private final DailyWellbeingRepository wellbeingRepository;
    private final PregnancyMilestoneRepository milestoneRepository;
    private final SymptomEntryRepository symptomRepository;

    public HealthcareReportController(UserRepository userRepository,
            PregnancyProfileRepository pregnancyProfileRepository,
            AppointmentRepository appointmentRepository, CareTaskRepository careTaskRepository,
            DailyWellbeingRepository wellbeingRepository, PregnancyMilestoneRepository milestoneRepository,
            SymptomEntryRepository symptomRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.wellbeingRepository = wellbeingRepository;
        this.milestoneRepository = milestoneRepository;
        this.symptomRepository = symptomRepository;
    }

    @GetMapping("/pregnancy-summary")
    public HealthcareReport pregnancySummary(Authentication authentication,
            @RequestParam(defaultValue = "false") boolean includeJournal) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return pregnancySummaryFor(user, includeJournal);
    }

    HealthcareReport pregnancySummaryFor(User user, boolean includeJournal) {
        PregnancyProfile profile = pregnancyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Create a pregnancy profile before generating a report"));
        LocalDate today = LocalDate.now();
        LocalDate trackingFrom = today.minusDays(29);
        List<CareTask> tasks = careTaskRepository
                .findByUserIdAndTaskDateBetweenOrderByTaskDateAscIdAsc(user.getId(), trackingFrom, today);
        List<DailyWellbeing> wellbeing = wellbeingRepository
                .findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(user.getId(), trackingFrom, today);

        return new HealthcareReport(OffsetDateTime.now(), includeJournal,
            new PersonSummary(user.getDisplayName(), user.getEmail()), pregnancy(profile),
            appointmentRepository.findByUserIdOrderByStartsAtAsc(user.getId()).stream()
                .map(appointment -> new AppointmentSummary(appointment.getTitle(), appointment.getStartsAt(),
                    appointment.getProviderName(), appointment.getClinicName(), appointment.getNotes())).toList(),
            tracking(tasks, wellbeing, trackingFrom, today),
            milestoneRepository.findByUserIdOrderByWeekNumberAsc(user.getId()).stream()
                .map(milestone -> new MilestoneSummary(milestone.getTitle(), milestone.getWeekNumber(),
                    milestone.isCompleted())).toList(),
            includeJournal ? symptomRepository.findByUserIdOrderByOccurredAtDesc(user.getId()).stream()
                .map(entry -> new JournalSummary(entry.getSymptom(), entry.getSeverity(), entry.getOccurredAt(),
                    entry.getNotes())).toList() : List.of(),
            "This report summarizes information recorded by the user in MaatriCare. It is not a medical record, diagnosis, or treatment recommendation.");
    }

    private PregnancySummary pregnancy(PregnancyProfile profile) {
        int week = (int) Math.max(0, Math.min(40,
                ChronoUnit.DAYS.between(profile.getLastMenstrualPeriod(), LocalDate.now()) / 7));
        String trimester = week < 14 ? "First trimester" : week < 28 ? "Second trimester" : "Third trimester";
        return new PregnancySummary(profile.getLastMenstrualPeriod(), profile.getDueDate(), week, trimester,
                profile.getAgeYears(), profile.getHeightCm(), profile.getPrePregnancyWeightKg(),
                profile.getBloodPressure(), profile.getBloodGroup());
    }

    private TrackingSummary tracking(List<CareTask> tasks, List<DailyWellbeing> wellbeing,
            LocalDate from, LocalDate to) {
        long completedTasks = tasks.stream().filter(CareTask::isCompleted).count();
        int completionPercentage = tasks.isEmpty() ? 0 : (int) Math.round(completedTasks * 100.0 / tasks.size());
        double averageWater = wellbeing.stream().mapToInt(DailyWellbeing::getWaterGlasses).average().orElse(0);
        double averageActivity = wellbeing.stream().mapToInt(DailyWellbeing::getActivityMinutes).average().orElse(0);
        BigDecimal averageSleep = wellbeing.isEmpty() ? BigDecimal.ZERO : wellbeing.stream()
                .map(DailyWellbeing::getSleepHours).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(wellbeing.size()), 1, RoundingMode.HALF_UP);
        long vitaminDays = wellbeing.stream().filter(DailyWellbeing::isPrenatalVitaminTaken).count();
        return new TrackingSummary(from, to, tasks.size(), completedTasks, completionPercentage,
                BigDecimal.valueOf(averageWater).setScale(1, RoundingMode.HALF_UP),
                BigDecimal.valueOf(averageActivity).setScale(1, RoundingMode.HALF_UP), averageSleep, vitaminDays);
    }

    public record HealthcareReport(OffsetDateTime generatedAt, boolean journalIncluded, PersonSummary person,
            PregnancySummary pregnancy, List<AppointmentSummary> appointments, TrackingSummary tracking,
            List<MilestoneSummary> milestones, List<JournalSummary> journal, String disclaimer) {}
    public record PersonSummary(String displayName, String email) {}
    public record PregnancySummary(LocalDate lastMenstrualPeriod, LocalDate dueDate, int currentWeek,
            String trimester, Integer ageYears, BigDecimal heightCm, BigDecimal prePregnancyWeightKg,
            String bloodPressure, String bloodGroup) {}
    public record AppointmentSummary(String title, OffsetDateTime startsAt, String providerName,
            String clinicName, String notes) {}
    public record TrackingSummary(LocalDate from, LocalDate to, int totalTasks, long completedTasks,
            int completionPercentage, BigDecimal averageWaterGlasses, BigDecimal averageActivityMinutes,
            BigDecimal averageSleepHours, long prenatalVitaminDays) {}
    public record MilestoneSummary(String title, int weekNumber, boolean completed) {}
    public record JournalSummary(String symptom, String severity, OffsetDateTime occurredAt, String notes) {}
}