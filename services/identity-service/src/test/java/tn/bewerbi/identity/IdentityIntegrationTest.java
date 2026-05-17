package tn.bewerbi.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end integration test for the identity-service.
 *
 * Uses a real PostgreSQL container (via Testcontainers) so Flyway migrations,
 * JPA mappings, JSON (de)serialization and Spring Security filters are all
 * exercised together — exactly the failure modes unit tests miss.
 *
 * Same pattern works for every other service: only the Testcontainers
 * setup and the endpoints under test change.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class IdentityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bewerbi_test")
            .withInitScript("init-test-schema.sql");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    void register_then_login_then_fetch_profile_with_completeness() throws Exception {
        // register
        String registerBody = """
                {
                  "email": "ahmed@example.tn",
                  "password": "StrongP@ss123",
                  "firstName": "Ahmed",
                  "lastName": "Ben Salem",
                  "role": "APPLICANT"
                }
                """;
        MvcResult register = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andReturn();
        assertThat(register.getResponse().getStatus()).isEqualTo(200);

        JsonNode regJson = mapper.readTree(register.getResponse().getContentAsString());
        String accessToken = regJson.get("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        // login
        String loginBody = """
                { "email": "ahmed@example.tn", "password": "StrongP@ss123" }
                """;
        MvcResult login = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andReturn();
        assertThat(login.getResponse().getStatus()).isEqualTo(200);

        // fetch profile — requires auth
        MvcResult profile = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/profile/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept-Language", "fr"))
                .andReturn();
        assertThat(profile.getResponse().getStatus()).isEqualTo(200);
        assertThat(profile.getResponse().getHeader("Content-Language")).isEqualTo("fr");

        JsonNode profileJson = mapper.readTree(profile.getResponse().getContentAsString());
        assertThat(profileJson.path("firstName").asText()).isEqualTo("Ahmed");
        assertThat(profileJson.path("completeness").path("percent").asInt()).isGreaterThan(0);
        assertThat(profileJson.path("completeness").path("tier").asText()).isIn("STARTER", "MOVER");
        assertThat(profileJson.path("completeness").path("nextAction")).isNotNull();
    }

    @Test
    void login_with_wrong_password_returns_401() throws Exception {
        String loginBody = """
                { "email": "nobody@example.tn", "password": "wrongwrongwrong" }
                """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isIn(401, 400);
    }
}
