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
    void weeklyGuideUsesAuthenticatedUsersCurrentPregnancyWeek() throws Exception {
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"guide@example.com\",\"password\":\"secure-password\",\"displayName\":\"Guide User\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();
        String lastMenstrualPeriod = java.time.LocalDate.now().minusWeeks(20).toString();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/profile/pregnancy")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"lastMenstrualPeriod\":\"" + lastMenstrualPeriod + "\",\"ageYears\":28}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/weekly-guide").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.week").value(20))
            .andExpect(jsonPath("$.trimester").value("Second trimester"))
            .andExpect(jsonPath("$.nutrition").isArray())
            .andExpect(jsonPath("$.activity").isArray())
            .andExpect(jsonPath("$.questionsForClinician").isArray())
            .andExpect(jsonPath("$.contentVersion").value("1.0"))
            .andExpect(jsonPath("$.reviewedOn").exists());
    }

    @Test
    void resourceLibrarySupportsCategoryAndTextFiltering() throws Exception {
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"resources@example.com\",\"password\":\"secure-password\",\"displayName\":\"Resource User\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/resources?category=Postpartum&q=recovery")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value("postpartum-recovery"))
            .andExpect(jsonPath("$[0].keyPoints").isArray())
            .andExpect(jsonPath("$[0].reviewStatus").value("EDUCATIONAL_REVIEW"));

        mockMvc.perform(get("/api/resources")).andExpect(status().isUnauthorized());
    }

    @Test
    void healthcareReportRequiresExplicitJournalConsent() throws Exception {
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"report@example.com\",\"password\":\"secure-password\",\"displayName\":\"Report User\"}"))
            .andExpect(status().isCreated()).andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/profile/pregnancy").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"lastMenstrualPeriod\":\"" + java.time.LocalDate.now().minusWeeks(22) + "\",\"ageYears\":28}"))
            .andExpect(status().isCreated());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/symptoms").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"symptom\":\"Tired\",\"severity\":\"MILD\",\"occurredAt\":\"2026-09-26T10:00:00+05:30\",\"notes\":\"Private note\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/pregnancy-summary").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.journalIncluded").value(false))
            .andExpect(jsonPath("$.journal.length()").value(0));
        mockMvc.perform(get("/api/reports/pregnancy-summary?includeJournal=true")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.journalIncluded").value(true))
            .andExpect(jsonPath("$.journal[0].notes").value("Private note"));
    }

    @Test
    void reportShareCanBeOpenedAnonymouslyAndRevokedByOwner() throws Exception {
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"share@example.com\",\"password\":\"secure-password\",\"displayName\":\"Share User\"}"))
            .andExpect(status().isCreated()).andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/profile/pregnancy").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"lastMenstrualPeriod\":\"" + java.time.LocalDate.now().minusWeeks(18) + "\",\"ageYears\":29}"))
            .andExpect(status().isCreated());

        MvcResult created = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/reports/shares?expiresInHours=24&includeJournal=false")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.journalIncluded").value(false)).andReturn();
        JsonNode createdShare = objectMapper.readTree(created.getResponse().getContentAsString());
        String shareId = createdShare.get("id").asText();
        String shareToken = createdShare.get("token").asText();

        mockMvc.perform(get("/api/shared-reports/" + shareToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.person.email").value("share@example.com"));
        mockMvc.perform(get("/api/reports/shares").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("ACTIVE"))
            .andExpect(jsonPath("$[0].accessCount").value(1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/reports/shares/" + shareId).header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/shared-reports/" + shareToken))
            .andExpect(status().isGone());
    }

    @Test
    void authenticatedUserReceivesSafeAssistantGuidance() throws Exception {
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"assistant@example.com\",\"password\":\"secure-password\",\"displayName\":\"Assistant User\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/assistant")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"question\":\"How can I prepare for an appointment?\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.source").value("LOCAL_MODEL_UNAVAILABLE"))
            .andExpect(jsonPath("$.urgent").value(false));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/assistant")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"question\":\"I have heavy bleeding\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.source").value("SAFETY_GUIDANCE"))
            .andExpect(jsonPath("$.urgent").value(true));
    }

    @Test
    void repeatedFailedLoginsAreTemporarilyBlocked() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"locked@example.com\",\"password\":\"correct-password\",\"displayName\":\"Locked User\"}"))
            .andExpect(status().isCreated());

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"locked@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"locked@example.com\",\"password\":\"correct-password\"}"))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void successfulLoginClearsPreviousFailures() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"recover@example.com\",\"password\":\"correct-password\",\"displayName\":\"Recover User\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"recover@example.com\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"recover@example.com\",\"password\":\"correct-password\"}"))
            .andExpect(status().isOk());

        for (int attempt = 0; attempt < 4; attempt++) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"recover@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void apiResponsesIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Referrer-Policy", "no-referrer"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("X-Frame-Options", "DENY"));
    }

    @Test
    void labGuideUsesProvidedRangesWithoutDiagnosingOrPrescribing() throws Exception {
        String token = registerAndGetToken("lab-guide@example.com", "Lab Guide User");
        String request = """
            {"values":[
              {"testName":"Vitamin C","value":0.2,"unit":"mg/dL","referenceMin":0.4,"referenceMax":2.0},
              {"testName":"Ferritin","value":45,"unit":"ng/mL","referenceMin":15,"referenceMax":150},
              {"testName":"Unknown marker","value":12,"unit":"U/L","referenceMin":20,"referenceMax":40}
            ]}
            """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/lab-guide/analyze").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results[0].status").value("BELOW_RANGE"))
            .andExpect(jsonPath("$.results[0].nutrient").value("Vitamin C"))
            .andExpect(jsonPath("$.results[0].foodSources[0]").value("oranges or other citrus fruit"))
            .andExpect(jsonPath("$.results[1].status").value("WITHIN_RANGE"))
            .andExpect(jsonPath("$.results[1].foodSources.length()").value(0))
            .andExpect(jsonPath("$.results[2].status").value("BELOW_RANGE"))
            .andExpect(jsonPath("$.results[2].foodSources.length()").value(0))
            .andExpect(jsonPath("$.disclaimer").value(org.hamcrest.Matchers.containsString("does not diagnose")));
    }

    @Test
    void userCanPersistAGentleDayPlanForToday() throws Exception {
        String token = registerAndGetToken("gentle-plan@example.com", "Gentle Plan User");
        MvcResult tasks = mockMvc.perform(get("/api/tasks?date=2026-09-27")
            .header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andReturn();
        String focusTaskId = objectMapper.readTree(tasks.getResponse().getContentAsString()).get(0).get("id").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/wellbeing/day-plan").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\":\"2026-09-27\",\"energyLevel\":\"LOW\",\"focusTaskCount\":1,\"focusTaskIds\":[\"" + focusTaskId + "\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.energyLevel").value("LOW"))
            .andExpect(jsonPath("$.focusTaskCount").value(1))
            .andExpect(jsonPath("$.focusTaskIds[0]").value(focusTaskId));

        mockMvc.perform(get("/api/wellbeing?date=2026-09-27")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.energyLevel").value("LOW"))
                .andExpect(jsonPath("$.focusTaskCount").value(1))
                .andExpect(jsonPath("$.focusTaskIds[0]").value(focusTaskId));
    }

    @Test
    void usersCannotAccessOrMutateAnotherUsersResources() throws Exception {
        String ownerToken = registerAndGetToken("owner@example.com", "Owner User");
        String otherToken = registerAndGetToken("other@example.com", "Other User");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/profile/pregnancy").header("Authorization", "Bearer " + ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"lastMenstrualPeriod\":\"" + java.time.LocalDate.now().minusWeeks(20) + "\",\"ageYears\":30}"))
            .andExpect(status().isCreated());

        MvcResult appointment = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/appointments").header("Authorization", "Bearer " + ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Owner appointment\",\"startsAt\":\"2026-10-10T10:00:00+05:30\"}"))
            .andExpect(status().isCreated()).andReturn();
        String appointmentId = objectMapper.readTree(appointment.getResponse().getContentAsString()).get("id").asText();

        MvcResult task = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/tasks").header("Authorization", "Bearer " + ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Owner-only custom task\",\"taskDate\":\"2026-09-26\",\"recurrence\":\"NONE\"}"))
            .andExpect(status().isCreated()).andReturn();
        String taskId = objectMapper.readTree(task.getResponse().getContentAsString()).get("id").asText();

        MvcResult symptom = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/symptoms").header("Authorization", "Bearer " + ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"symptom\":\"Private symptom\",\"severity\":\"MILD\",\"occurredAt\":\"2026-09-26T10:00:00+05:30\",\"notes\":\"Owner note\"}"))
            .andExpect(status().isCreated()).andReturn();
        String symptomId = objectMapper.readTree(symptom.getResponse().getContentAsString()).get("id").asText();

        MvcResult milestones = mockMvc.perform(get("/api/pregnancy-milestones")
            .header("Authorization", "Bearer " + ownerToken)).andExpect(status().isOk()).andReturn();
        String milestoneId = objectMapper.readTree(milestones.getResponse().getContentAsString()).get(0).get("id").asText();

        MvcResult share = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/reports/shares").header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isCreated()).andReturn();
        String shareId = objectMapper.readTree(share.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/symptoms").header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/reports/shares").header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/appointments/" + appointmentId).header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Stolen appointment\",\"startsAt\":\"2026-10-11T10:00:00+05:30\"}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/appointments/" + appointmentId + "/questions").header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"Unauthorized question\"}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/tasks/" + taskId + "/complete").header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":true}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/symptoms/" + symptomId).header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"symptom\":\"Changed\",\"severity\":\"SEVERE\",\"occurredAt\":\"2026-09-26T11:00:00+05:30\"}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/pregnancy-milestones/" + milestoneId + "/complete")
            .header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":true}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/reports/shares/" + shareId).header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/profile").header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Changed Other\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Owner User"));
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

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/wellbeing/mood")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-09-16\",\"mood\":\"Feeling well\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mood").value("Feeling well"));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/wellbeing/sleep")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-09-16\",\"hours\":7.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepHours").value(7.5))
                .andExpect(jsonPath("$.sleepGoal").value(8.0));

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

    private String registerAndGetToken(String email, String displayName) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "email", email, "password", "secure-password", "displayName", displayName));
        MvcResult registration = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();
    }
}
