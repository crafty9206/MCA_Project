package com.maatricare.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.auth.PasswordResetTokenRepository;
import com.maatricare.tracking.AppointmentRepository;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.tracking.DailyWellbeingRepository;
import com.maatricare.tracking.SymptomEntryRepository;
import com.maatricare.pregnancy.PregnancyMilestoneRepository;
import com.maatricare.profile.ReportShareRepository;
import com.maatricare.profile.EmergencyContactRepository;
import com.maatricare.notifications.PushReminderRepository;
import com.maatricare.notifications.PushSubscriptionRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final CareTaskRepository careTaskRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final DailyWellbeingRepository dailyWellbeingRepository;
    private final SymptomEntryRepository symptomEntryRepository;
    private final PregnancyMilestoneRepository pregnancyMilestoneRepository;
    private final ReportShareRepository reportShareRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final PushReminderRepository pushReminderRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public AccountService(UserRepository userRepository, PregnancyProfileRepository pregnancyProfileRepository,
            AppointmentRepository appointmentRepository, CareTaskRepository careTaskRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            DailyWellbeingRepository dailyWellbeingRepository, SymptomEntryRepository symptomEntryRepository,
            PregnancyMilestoneRepository pregnancyMilestoneRepository, ReportShareRepository reportShareRepository,
            EmergencyContactRepository emergencyContactRepository, PushReminderRepository pushReminderRepository,
            PushSubscriptionRepository pushSubscriptionRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.dailyWellbeingRepository = dailyWellbeingRepository;
        this.symptomEntryRepository = symptomEntryRepository;
        this.pregnancyMilestoneRepository = pregnancyMilestoneRepository;
        this.reportShareRepository = reportShareRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.pushReminderRepository = pushReminderRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    @Transactional
    public void deleteByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));
        passwordResetTokenRepository.deleteByUserId(user.getId());
        emergencyContactRepository.deleteByUserId(user.getId());
        pushReminderRepository.deleteAll(pushReminderRepository.findByUserIdAndStatusIn(user.getId(),
            java.util.List.of("PENDING", "SENDING", "SENT", "CANCELLED", "FAILED")));
        pushSubscriptionRepository.deleteByUserId(user.getId());
        reportShareRepository.deleteByUserId(user.getId());
        dailyWellbeingRepository.deleteByUserId(user.getId());
        symptomEntryRepository.deleteByUserId(user.getId());
        pregnancyMilestoneRepository.deleteByUserId(user.getId());
        careTaskRepository.deleteByUserId(user.getId());
        appointmentRepository.deleteByUserId(user.getId());
        pregnancyProfileRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }
}