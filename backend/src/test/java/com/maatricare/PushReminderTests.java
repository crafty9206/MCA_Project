package com.maatricare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maatricare.notifications.PushReminder;
import com.maatricare.notifications.PushReminderRepository;
import com.maatricare.notifications.PushReminderSchedulingService;
import com.maatricare.notifications.PushSubscription;
import com.maatricare.notifications.PushSubscriptionRepository;
import com.maatricare.pregnancy.PregnancyProfile;
import com.maatricare.pregnancy.PregnancyProfileRepository;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PushReminderTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PushReminderRepository reminderRepository;
    @Autowired private PushSubscriptionRepository subscriptionRepository;
    @Autowired private PregnancyProfileRepository pregnancyProfileRepository;
    @Autowired private PushReminderSchedulingService schedulingService;

    @Test
    void profileUsesDelhiTimezoneAndRejectsUnsupportedTimezones() throws Exception {
        String token = registerAndGetToken("timezone@example.com", "Timezone User");

        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezoneId").value("Asia/Kolkata"))
                .andExpect(jsonPath("$.pushNotificationsEnabled").value(false));
        mockMvc.perform(get("/api/push/settings").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.timezoneId").value("Asia/Kolkata"));
        mockMvc.perform(patch("/api/profile").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Timezone User\",\"timezoneId\":\"Not/AZone\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void scheduledRemindersUseDelhiWallTimeAndRespectCategoryPreferences() {
        ZoneId delhi = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(delhi);
        User user = new User("scheduler-" + UUID.randomUUID() + "@example.com", "hash", "Scheduler User");
        user.setTimezoneId("Asia/Kolkata");
        user.setPushNotificationsEnabled(true);
        user.updateNotificationPreferences(false, false, false, LocalTime.of(10, 0),
                true, today.getDayOfWeek().getValue(), LocalTime.of(9, 17), false);
        user = userRepository.saveAndFlush(user);
        subscriptionRepository.saveAndFlush(new PushSubscription(user, "https://fcm.googleapis.com/test",
                UUID.randomUUID().toString().replace("-", ""), "p256dh", "auth"));
        pregnancyProfileRepository.saveAndFlush(new PregnancyProfile(user, today.minusDays(140),
                today.plusDays(140), 29, null, null, null, null));

        schedulingService.synchronizeAndFindDue();

        String key = "pregnancy:" + today + ":" + today.getDayOfWeek().getValue() + ":09:17";
        PushReminder reminder = reminderRepository.findByUserIdAndDeduplicationKey(user.getId(), key).orElseThrow();
        assertEquals(today.atTime(9, 17).atZone(delhi).toInstant(), reminder.getScheduledAt());
        assertEquals("PENDING", reminder.getStatus());
        assertTrue(reminderRepository.findByUserIdAndStatusIn(user.getId(), java.util.List.of("PENDING"))
                .stream().noneMatch(item -> item.getReminderType().equals("CARE")));

        schedulingService.synchronizeAndFindDue();
        assertEquals(1L, reminderRepository.findByUserIdAndStatusIn(user.getId(), java.util.List.of("PENDING"))
                .stream().filter(item -> item.getDeduplicationKey().equals(key)).count());

        user.updateNotificationPreferences(false, false, false, LocalTime.of(10, 0),
                false, today.getDayOfWeek().getValue(), LocalTime.of(9, 17), false);
        userRepository.saveAndFlush(user);
        schedulingService.synchronizeAndFindDue();
        assertEquals("CANCELLED", reminderRepository.findById(reminder.getId()).orElseThrow().getStatus());
    }

    @Test
    void notificationSnoozeCapabilityIsOneTimeAndDoesNotRequireAnAccountToken() throws Exception {
        User user = userRepository.saveAndFlush(new User("snooze-" + UUID.randomUUID() + "@example.com",
                "hash", "Snooze User"));
        String key = "care:2026-10-15:09:00";
        String token = "A".repeat(43);
        Instant now = Instant.now();
        PushReminder reminder = new PushReminder(user, key, "CARE", "Daily care reminder",
                "Open MaatriCare to review your care tasks.", now.minusSeconds(1));
        reminder.markClaimed(now, sha256(token), now.plusSeconds(7 * 24 * 60 * 60L));
        reminder.markDelivered(now);
        reminder = reminderRepository.saveAndFlush(reminder);

        String body = objectMapper.writeValueAsString(java.util.Map.of("deduplicationKey", key, "snoozeToken", token));
        mockMvc.perform(post("/api/push/snooze-action").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        PushReminder snoozed = reminderRepository.findById(reminder.getId()).orElseThrow();
        assertEquals("PENDING", snoozed.getStatus());
        assertNotNull(snoozed.getSnoozedUntil());
        assertTrue(snoozed.getSnoozedUntil().isAfter(Instant.now().plusSeconds(59 * 60)));
        mockMvc.perform(post("/api/push/snooze-action").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    private String registerAndGetToken(String email, String displayName) throws Exception {
        var result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email, "password", "secure-password", "displayName", displayName))))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String sha256(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes);
    }
}
