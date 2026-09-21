package com.maatricare;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maatricare.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MaatriCareApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void healthEndpointReturnsRunningStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void authenticatedUserCanDeleteAccountAndOwnedData() throws Exception {
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"delete-me@example.com\",\"password\":\"secure-password\",\"displayName\":\"Delete Me\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/profile/pregnancy")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"lastMenstrualPeriod\":\"2026-03-01\",\"ageYears\":28}"))
            .andExpect(status().isCreated());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/auth/account")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(userRepository.existsByEmailIgnoreCase("delete-me@example.com"));
        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanResetPasswordWithOneTimeToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"reset@example.com\",\"password\":\"old-password\",\"displayName\":\"Reset User\"}"))
            .andExpect(status().isCreated());

        MvcResult forgot = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/forgot-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"reset@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists())
            .andReturn();
        String resetToken = objectMapper.readTree(forgot.getResponse().getContentAsString()).get("resetToken").asText();

        String resetBody = objectMapper.writeValueAsString(java.util.Map.of(
                "token", resetToken, "password", "New-password-1"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(resetBody))
            .andExpect(status().isNoContent());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"reset@example.com\",\"password\":\"old-password\"}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"reset@example.com\",\"password\":\"New-password-1\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(resetBody))
            .andExpect(status().isBadRequest());
    }

            @Test
            void registrationAndPregnancyProfileFlowRequiresJwt() throws Exception {
            mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

            MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ananya@example.com\",\"password\":\"secure-password\",\"displayName\":\"Ananya\"}"))
                .andExpect(status().isCreated())
                .andReturn();

            JsonNode response = objectMapper.readTree(registration.getResponse().getContentAsString());
            String token = response.get("token").asText();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/profile/pregnancy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastMenstrualPeriod\":\"2026-03-01\",\"ageYears\":28}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ananya@example.com"))
                .andExpect(jsonPath("$.pregnancy.dueDate").value("2026-12-06"));

            mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pregnancy.trimester").exists());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Ananya Sharma\",\"appointmentRemindersEnabled\":true,\"browserNotificationsEnabled\":true,\"dailyCareRemindersEnabled\":true,\"dailyCareReminderTime\":\"08:30\",\"weeklyPregnancyRemindersEnabled\":true,\"weeklyPregnancyReminderDay\":6,\"weeklyPregnancyReminderTime\":\"10:15\",\"missedTaskRemindersEnabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Ananya Sharma"))
                .andExpect(jsonPath("$.appointmentRemindersEnabled").value(true))
                .andExpect(jsonPath("$.browserNotificationsEnabled").value(true))
                .andExpect(jsonPath("$.dailyCareRemindersEnabled").value(true))
                .andExpect(jsonPath("$.dailyCareReminderTime").value("08:30:00"))
                .andExpect(jsonPath("$.weeklyPregnancyRemindersEnabled").value(true))
                .andExpect(jsonPath("$.weeklyPregnancyReminderDay").value(6))
                .andExpect(jsonPath("$.weeklyPregnancyReminderTime").value("10:15:00"))
                .andExpect(jsonPath("$.missedTaskRemindersEnabled").value(true));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/profile/pregnancy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastMenstrualPeriod\":\"2026-03-02\",\"ageYears\":29}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pregnancy.dueDate").value("2026-12-07"));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
            }

            @Test
            void authenticatedUserCanCreateAppointmentAndCompleteTask() throws Exception {
            MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"tracking@example.com\",\"password\":\"secure-password\",\"displayName\":\"Tracking User\"}"))
                .andExpect(status().isCreated())
                .andReturn();
            String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();

            mockMvc.perform(get("/api/wellbeing?date=2026-09-16")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waterGlasses").value(0))
                .andExpect(jsonPath("$.waterGoal").value(8));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/wellbeing/water")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-09-16\",\"glasses\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waterGlasses").value(5));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/wellbeing/prenatal-vitamin")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-09-16\",\"taken\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenatalVitaminTaken").value(true));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/wellbeing/activity")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-09-16\",\"minutes\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityMinutes").value(15))
                .andExpect(jsonPath("$.activityGoal").value(20));

            MvcResult appointment = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Mid-pregnancy checkup\",\"startsAt\":\"2026-09-17T10:30:00+05:30\",\"providerName\":\"Dr. Meera Shah\",\"reminderMinutesBefore\":1440}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mid-pregnancy checkup"))
                .andExpect(jsonPath("$.reminderMinutesBefore").value(1440))
                .andReturn();
            String appointmentId = objectMapper.readTree(appointment.getResponse().getContentAsString()).get("id").asText();

            MvcResult question = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/appointments/" + appointmentId + "/questions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"Which symptoms should I monitor?\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answered").value(false))
                .andReturn();
            String questionId = objectMapper.readTree(question.getResponse().getContentAsString()).get("id").asText();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/appointment-questions/" + questionId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"Which symptoms should I monitor?\",\"answered\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answered").value(true));

            mockMvc.perform(get("/api/appointments/" + appointmentId + "/questions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").value("Which symptoms should I monitor?"));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/appointments/" + appointmentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated checkup\",\"startsAt\":\"2026-09-17T11:00:00+05:30\",\"reminderMinutesBefore\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated checkup"))
                .andExpect(jsonPath("$.reminderMinutesBefore").value(60));

            MvcResult task = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Take prenatal vitamins\",\"taskDate\":\"2026-09-16\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn();
            String taskId = objectMapper.readTree(task.getResponse().getContentAsString()).get("id").asText();
            String taskDetailId = objectMapper.readTree(task.getResponse().getContentAsString()).get("taskDetailId").asText();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/tasks/" + taskId + "/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

            MvcResult firstUserTasks = mockMvc.perform(get("/api/tasks?from=2026-09-16&to=2026-09-16")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
            JsonNode firstUserTask = findTaskByDetailId(firstUserTasks, taskDetailId);
            org.junit.jupiter.api.Assertions.assertTrue(firstUserTask.get("completed").asBoolean());

            MvcResult secondRegistration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"second-user@example.com\",\"password\":\"secure-password\",\"displayName\":\"Second User\"}"))
                .andExpect(status().isCreated())
                .andReturn();
            String secondToken = objectMapper.readTree(secondRegistration.getResponse().getContentAsString()).get("token").asText();
            MvcResult secondUserTasks = mockMvc.perform(get("/api/tasks?date=2026-09-16")
                .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andReturn();
            JsonNode secondUserTask = findTaskByDetailId(secondUserTasks, taskDetailId);
            org.junit.jupiter.api.Assertions.assertFalse(secondUserTask.get("completed").asBoolean());
            org.junit.jupiter.api.Assertions.assertNotEquals(firstUserTask.get("id").asText(), secondUserTask.get("id").asText());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/appointments/" + appointmentId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
            }

    private JsonNode findTaskByDetailId(MvcResult result, String taskDetailId) throws Exception {
        for (JsonNode task : objectMapper.readTree(result.getResponse().getContentAsString())) {
            if (taskDetailId.equals(task.get("taskDetailId").asText())) return task;
        }
        throw new AssertionError("Task detail was not returned");
    }
}
