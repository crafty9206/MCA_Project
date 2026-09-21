package com.maatricare.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.auth.PasswordResetTokenRepository;
import com.maatricare.tracking.AppointmentRepository;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.tracking.DailyWellbeingRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final CareTaskRepository careTaskRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final DailyWellbeingRepository dailyWellbeingRepository;

    public AccountService(UserRepository userRepository, PregnancyProfileRepository pregnancyProfileRepository,
            AppointmentRepository appointmentRepository, CareTaskRepository careTaskRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            DailyWellbeingRepository dailyWellbeingRepository) {
        this.userRepository = userRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.dailyWellbeingRepository = dailyWellbeingRepository;
    }

    @Transactional
    public void deleteByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));
        passwordResetTokenRepository.deleteByUserId(user.getId());
        dailyWellbeingRepository.deleteByUserId(user.getId());
        careTaskRepository.deleteByUserId(user.getId());
        appointmentRepository.deleteByUserId(user.getId());
        pregnancyProfileRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }
}