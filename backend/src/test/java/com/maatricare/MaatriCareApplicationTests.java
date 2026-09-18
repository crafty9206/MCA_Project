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
                .content("{\"lastMenstrualPeriod\":\"2026-03-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ananya@example.com"))
                .andExpect(jsonPath("$.pregnancy.dueDate").value("2026-12-06"));

            mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pregnancy.trimester").exists());
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

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Mid-pregnancy checkup\",\"startsAt\":\"2026-09-17T10:30:00+05:30\",\"providerName\":\"Dr. Meera Shah\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mid-pregnancy checkup"));

            MvcResult task = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Take prenatal vitamins\",\"taskDate\":\"2026-09-16\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn();
            String taskId = objectMapper.readTree(task.getResponse().getContentAsString()).get("id").asText();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/tasks/" + taskId + "/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
            }
}
