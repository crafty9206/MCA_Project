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

    public AccountService(UserRepository userRepository, PregnancyProfileRepository pregnancyProfileRepository,
            AppointmentRepository appointmentRepository, CareTaskRepository careTaskRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            DailyWellbeingRepository dailyWellbeingRepository, SymptomEntryRepository symptomEntryRepository,
            PregnancyMilestoneRepository pregnancyMilestoneRepository, ReportShareRepository reportShareRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.dailyWellbeingRepository = dailyWellbeingRepository;
        this.symptomEntryRepository = symptomEntryRepository;
        this.pregnancyMilestoneRepository = pregnancyMilestoneRepository;
        this.reportShareRepository = reportShareRepository;
    }

    @Transactional
    public void deleteByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));
        passwordResetTokenRepository.deleteByUserId(user.getId());
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