package com.quizora.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizora.backend.domain.AttemptStatus;
import com.quizora.backend.repository.QuizAttemptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * End-to-end smoke test of the Phase 1 MVP flow against the seeded demo data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class QuizFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuizAttemptRepository attemptRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private String bearer(String token) { return "Bearer " + token; }

    private String register(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test Learner\",\"email\":\"" + email
                                + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    @DisplayName("register -> courses -> one-question quiz -> submit -> progress -> dashboard")
    void fullQuizFlow() throws Exception {
        String token = register("learner" + System.nanoTime() + "@test.com", "Secret123");

        // free trial starts today and lasts 7 days (quizora.subscription.trial-days)
        mockMvc.perform(get("/api/subscriptions/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("TRIAL"))
                .andExpect(jsonPath("$.startDate").value(java.time.LocalDate.now().toString()))
                .andExpect(jsonPath("$.endDate").value(java.time.LocalDate.now().plusDays(7).toString()));

        // courses page
        MvcResult coursesResult = mockMvc.perform(get("/api/courses").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].years").isArray())
                .andReturn();
        JsonNode courses = objectMapper.readTree(coursesResult.getResponse().getContentAsString());
        assertThat(courses.size()).isGreaterThanOrEqualTo(5);
        long courseId = courses.get(0).get("id").asLong();
        int year = courses.get(0).get("years").get(0).asInt();

        // start an attempt
        MvcResult startResult = mockMvc.perform(post("/api/quizzes/start")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + ",\"year\":" + year + ",\"timed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").isNumber())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn();
        JsonNode start = objectMapper.readTree(startResult.getResponse().getContentAsString());
        long attemptId = start.get("attemptId").asLong();
        int totalQuestions = start.get("totalQuestions").asInt();
        assertThat(totalQuestions).isGreaterThan(0);

        // one question at a time - correct answer must NOT be revealed yet
        MvcResult questionResult = mockMvc.perform(
                        get("/api/quizzes/{id}/questions/{index}", attemptId, 0)
                                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").isNumber())
                .andExpect(jsonPath("$.optionA").isNotEmpty())
                .andExpect(jsonPath("$.correctOption").doesNotExist())
                .andReturn();
        long questionId = objectMapper.readTree(questionResult.getResponse().getContentAsString())
                .get("questionId").asLong();

        // record an answer
        mockMvc.perform(post("/api/quizzes/{id}/answers", attemptId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":" + questionId
                                + ",\"selectedOption\":\"A\",\"timeSpentSeconds\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(true));

        // submit for grading
        MvcResult submitResult = mockMvc.perform(post("/api/quizzes/{id}/submit", attemptId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.answeredCount").value(1))
                .andReturn();
        JsonNode result = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        assertThat(result.get("review").size()).isEqualTo(totalQuestions);
        // review reveals correct answers + explanation
        assertThat(result.get("review").get(0).has("correctOption")).isTrue();

        // submitting twice is idempotent
        mockMvc.perform(post("/api/quizzes/{id}/submit", attemptId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // progress page
        mockMvc.perform(get("/api/progress").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.questionsAttempted").value(1))
                .andExpect(jsonPath("$.perCourse").isArray());

        // dashboard: greeting + daily quote + analytics
        mockMvc.perform(get("/api/dashboard").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").isNotEmpty())
                .andExpect(jsonPath("$.quote.text").isNotEmpty())
                .andExpect(jsonPath("$.analytics.questionsAttempted").value(1));

        // unauthenticated access is rejected
        mockMvc.perform(get("/api/courses")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/stats")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("institutional code onboarding + institution report export")
    void institutionalFlow() throws Exception {
        String studentEmail = "linked" + System.nanoTime() + "@test.com";
        MvcResult regResult = mockMvc.perform(post("/api/auth/register-institutional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DEMO-CODE-1234\",\"fullName\":\"Ama Student\","
                                + "\"email\":\"" + studentEmail + "\",\"password\":\"Secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.institutionName").value("Demo JHS Accra"))
                .andReturn();
        String studentToken = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .get("token").asText();

        // the linked student now has an institutional subscription
        mockMvc.perform(get("/api/subscriptions/me").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("INSTITUTIONAL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // log in as the institution admin
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"school@demo.com\",\"password\":\"School@123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode login = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String adminToken = login.get("token").asText();
        long institutionId = login.get("user").get("institutionId").asLong();

        // student list includes the newly linked student
        MvcResult studentsResult = mockMvc.perform(
                        get("/api/institutions/{id}/students", institutionId)
                                .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode students = objectMapper.readTree(studentsResult.getResponse().getContentAsString());
        assertThat(students.size()).isGreaterThanOrEqualTo(2);

        // licence seat was consumed
        mockMvc.perform(get("/api/institutions/{id}", institutionId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenses[0].usedSeats").value(2));

        // CSV report download
        mockMvc.perform(get("/api/institutions/{id}/report/export", institutionId)
                        .param("format", "csv")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString(".csv")));

        // PDF report download
        mockMvc.perform(get("/api/institutions/{id}/report/export", institutionId)
                        .param("format", "pdf")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        // a student cannot reach admin endpoints
        mockMvc.perform(get("/api/admin/stats").header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        // ...nor create institutions (README: "admin: create institution + admin account")
        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Rogue JHS\",\"contactName\":\"Rogue\","
                                + "\"contactEmail\":\"rogue@jhs.com\",\"phone\":\"0200000000\","
                                + "\"adminPassword\":\"Secret123\"}"))
                .andExpect(status().isForbidden());

        // unauthenticated creation is rejected as 401
        mockMvc.perform(post("/api/institutions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Rogue JHS\",\"contactName\":\"Rogue\","
                                + "\"contactEmail\":\"rogue2@jhs.com\",\"phone\":\"0200000000\","
                                + "\"adminPassword\":\"Secret123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a timed attempt is committed as expired when the learner requests another question")
    void expiredAttemptIsPersisted() throws Exception {
        String token = register("expired" + System.nanoTime() + "@test.com", "Secret123");
        MvcResult coursesResult = mockMvc.perform(get("/api/courses")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode course = objectMapper.readTree(coursesResult.getResponse().getContentAsString()).get(0);

        MvcResult startResult = mockMvc.perform(post("/api/quizzes/start")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + course.get("id").asLong()
                                + ",\"year\":" + course.get("years").get(0).asInt()
                                + ",\"timed\":true,\"timeLimitSeconds\":30}"))
                .andExpect(status().isOk())
                .andReturn();
        long attemptId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .get("attemptId").asLong();

        jdbcTemplate.update("UPDATE quiz_attempts SET started_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusSeconds(31)), attemptId);

        mockMvc.perform(get("/api/quizzes/{id}/questions/{index}", attemptId, 0)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        assertThat(attemptRepository.findById(attemptId).orElseThrow().getStatus())
                .isEqualTo(AttemptStatus.EXPIRED);
    }
}
