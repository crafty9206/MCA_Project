package com.maatricare.notifications;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.tracking.Appointment;
import com.maatricare.tracking.AppointmentRepository;
import com.maatricare.tracking.CareTask;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushReminderSchedulingService {

    private static final int MAX_ATTEMPTS = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CareTaskRepository careTaskRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final PushSubscriptionRepository subscriptionRepository;
    private final PushReminderRepository reminderRepository;

    public PushReminderSchedulingService(UserRepository userRepository, AppointmentRepository appointmentRepository,
            CareTaskRepository careTaskRepository, PregnancyProfileRepository pregnancyProfileRepository,
            PushSubscriptionRepository subscriptionRepository, PushReminderRepository reminderRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.careTaskRepository = careTaskRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.reminderRepository = reminderRepository;
    }

    @Transactional
    public List<UUID> synchronizeAndFindDue() {
        Instant now = Instant.now();
        reminderRepository.releaseExpiredClaims(now.minusSeconds(300));
        for (User user : userRepository.findAll()) synchronizeUser(user, now);
        return reminderRepository.findTop50ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc("PENDING", now)
                .stream().map(PushReminder::getId).toList();
    }

    @Transactional
    public Delivery claim(UUID reminderId) {
        Instant now = Instant.now();
        if (reminderRepository.claim(reminderId, now) != 1) return null;
        PushReminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if (reminder == null) return null;
        byte[] secret = new byte[32];
        SECURE_RANDOM.nextBytes(secret);
        String snoozeToken = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        reminder.markClaimed(now, sha256(snoozeToken), now.plus(java.time.Duration.ofDays(7)));
        reminderRepository.save(reminder);
        return new Delivery(reminder.getId(), reminder.getUser().getId(), reminder.getDeduplicationKey(),
            reminder.getTitle(), reminder.getBody(), snoozeToken);
    }

    @Transactional
    public void finish(UUID reminderId, boolean delivered) {
        PushReminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if (reminder == null || !"SENDING".equals(reminder.getStatus())) return;
        Instant now = Instant.now();
        if (delivered) {
            reminder.markDelivered(now);
        } else if (reminder.getAttemptCount() + 1 >= MAX_ATTEMPTS) {
            reminder.markFailed(now);
        } else {
            long delaySeconds = Math.min(6 * 60 * 60L, 30L * (1L << Math.min(reminder.getAttemptCount(), 10)));
            reminder.retryAt(now.plusSeconds(delaySeconds));
        }
        reminderRepository.save(reminder);
    }

    private void synchronizeUser(User user, Instant now) {
        if (!user.isPushNotificationsEnabled() || subscriptionRepository.countByUserId(user.getId()) == 0) {
            cancelPending(user, now, Set.of());
            return;
        }
        ZoneId zone;
        try { zone = ZoneId.of(user.getTimezoneId()); }
        catch (RuntimeException exception) { zone = ZoneId.of("Asia/Kolkata"); }
        ZoneId userZone = zone;
        LocalDate localToday = LocalDate.now(zone);
        Map<String, ReminderSpec> desired = new HashMap<>();

        if (user.isAppointmentRemindersEnabled()) {
            for (Appointment appointment : appointmentRepository.findByUserIdOrderByStartsAtAsc(user.getId())) {
                if (appointment.getReminderMinutesBefore() == null || !appointment.getStartsAt().toInstant().isAfter(now)) continue;
                String key = "appointment:" + appointment.getId() + ":" + appointment.getStartsAt().toInstant().toEpochMilli()
                        + ":" + appointment.getReminderMinutesBefore();
                Instant scheduled = appointment.getStartsAt().toInstant()
                        .minus(appointment.getReminderMinutesBefore(), ChronoUnit.MINUTES);
                desired.put(key, new ReminderSpec(key, "APPOINTMENT", "Upcoming appointment",
                        "Open MaatriCare to view your appointment reminder.", scheduled));
            }
        }

        List<CareTask> todayTasks = careTaskRepository.findByUserIdAndTaskDateOrderByIdAsc(user.getId(), localToday);
        List<CareTask> unfinishedToday = todayTasks.stream().filter(task -> !task.isCompleted()).toList();
        if (user.isDailyCareRemindersEnabled() && !unfinishedToday.isEmpty()) {
            LocalTime reminderTime = user.getDailyCareReminderTime() == null ? LocalTime.of(9, 0) : user.getDailyCareReminderTime();
            String key = "care:" + localToday + ":" + reminderTime;
            desired.put(key, new ReminderSpec(key, "CARE", "Daily care reminder",
                    "Open MaatriCare to review your care tasks.", localToday.atTime(reminderTime).atZone(zone).toInstant()));
        }

        if (user.isWeeklyPregnancyRemindersEnabled()) {
            pregnancyProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                LocalDate monday = localToday.minusDays(localToday.getDayOfWeek().getValue() - 1L);
                LocalDate reminderDate = monday.plusDays(user.getWeeklyPregnancyReminderDay() - 1L);
                LocalTime reminderTime = user.getWeeklyPregnancyReminderTime() == null
                        ? LocalTime.of(9, 0) : user.getWeeklyPregnancyReminderTime();
                String key = "pregnancy:" + reminderDate + ":" + user.getWeeklyPregnancyReminderDay() + ":" + reminderTime;
                desired.put(key, new ReminderSpec(key, "PREGNANCY", "Weekly pregnancy update",
                        "Open MaatriCare to view your weekly update.",
                        reminderDate.atTime(reminderTime).atZone(userZone).toInstant()));
            });
        }

        if (user.isMissedTaskRemindersEnabled()) {
            LocalDate yesterday = localToday.minusDays(1);
            boolean hasMissedTasks = careTaskRepository.findByUserIdAndTaskDateOrderByIdAsc(user.getId(), yesterday)
                    .stream().anyMatch(task -> !task.isCompleted());
            if (hasMissedTasks) {
                String key = "missed:" + yesterday;
                LocalTime reminderTime = user.getDailyCareReminderTime() == null ? LocalTime.of(9, 0) : user.getDailyCareReminderTime();
                desired.put(key, new ReminderSpec(key, "MISSED", "Missed care tasks",
                        "Open MaatriCare to review yesterday’s care tasks.",
                        localToday.atTime(reminderTime).atZone(zone).toInstant()));
            }
        }

        cancelPending(user, now, desired.keySet());
        for (ReminderSpec spec : desired.values()) {
            PushReminder reminder = reminderRepository.findByUserIdAndDeduplicationKey(user.getId(), spec.key())
                    .orElseGet(() -> new PushReminder(user, spec.key(), spec.type(), spec.title(), spec.body(), spec.scheduledAt()));
            reminder.refresh(spec.type(), spec.title(), spec.body(), spec.scheduledAt());
            reminderRepository.save(reminder);
        }
    }

    private void cancelPending(User user, Instant now, Set<String> keepKeys) {
        for (PushReminder reminder : reminderRepository.findByUserIdAndStatusIn(user.getId(), List.of("PENDING"))) {
            if (!keepKeys.contains(reminder.getDeduplicationKey())) reminder.cancel(now);
        }
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record Delivery(UUID id, UUID userId, String key, String title, String body, String snoozeToken) {}
    private record ReminderSpec(String key, String type, String title, String body, Instant scheduledAt) {}
}