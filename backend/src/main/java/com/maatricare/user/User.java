package com.maatricare.user;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = "en";

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "appointment_reminders_enabled", nullable = false)
    private boolean appointmentRemindersEnabled = true;

    @Column(name = "browser_notifications_enabled", nullable = false)
    private boolean browserNotificationsEnabled;

    @Column(name = "daily_care_reminders_enabled", nullable = false)
    private boolean dailyCareRemindersEnabled;

    @Column(name = "daily_care_reminder_time", nullable = false)
    private LocalTime dailyCareReminderTime = LocalTime.of(9, 0);

    @Column(name = "weekly_pregnancy_reminders_enabled", nullable = false)
    private boolean weeklyPregnancyRemindersEnabled;

    @Column(name = "weekly_pregnancy_reminder_day", nullable = false)
    private int weeklyPregnancyReminderDay = 1;

    @Column(name = "weekly_pregnancy_reminder_time", nullable = false)
    private LocalTime weeklyPregnancyReminderTime = LocalTime.of(9, 0);

    @Column(name = "missed_task_reminders_enabled", nullable = false)
    private boolean missedTaskRemindersEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
    }

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    @jakarta.persistence.PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getRole() {
        return role;
    }

    public boolean isAppointmentRemindersEnabled() { return appointmentRemindersEnabled; }
    public boolean isBrowserNotificationsEnabled() { return browserNotificationsEnabled; }
    public boolean isDailyCareRemindersEnabled() { return dailyCareRemindersEnabled; }
    public LocalTime getDailyCareReminderTime() { return dailyCareReminderTime; }
    public boolean isWeeklyPregnancyRemindersEnabled() { return weeklyPregnancyRemindersEnabled; }
    public int getWeeklyPregnancyReminderDay() {
        return weeklyPregnancyReminderDay >= 1 && weeklyPregnancyReminderDay <= 7
                ? weeklyPregnancyReminderDay : 1;
    }
    public LocalTime getWeeklyPregnancyReminderTime() { return weeklyPregnancyReminderTime; }
    public boolean isMissedTaskRemindersEnabled() { return missedTaskRemindersEnabled; }

    public void updateNotificationPreferences(boolean appointmentRemindersEnabled,
            boolean browserNotificationsEnabled, boolean dailyCareRemindersEnabled,
            LocalTime dailyCareReminderTime, boolean weeklyPregnancyRemindersEnabled,
            int weeklyPregnancyReminderDay, LocalTime weeklyPregnancyReminderTime,
            boolean missedTaskRemindersEnabled) {
        this.appointmentRemindersEnabled = appointmentRemindersEnabled;
        this.browserNotificationsEnabled = browserNotificationsEnabled;
        this.dailyCareRemindersEnabled = dailyCareRemindersEnabled;
        this.dailyCareReminderTime = dailyCareReminderTime;
        this.weeklyPregnancyRemindersEnabled = weeklyPregnancyRemindersEnabled;
        this.weeklyPregnancyReminderDay = weeklyPregnancyReminderDay;
        this.weeklyPregnancyReminderTime = weeklyPregnancyReminderTime;
        this.missedTaskRemindersEnabled = missedTaskRemindersEnabled;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}