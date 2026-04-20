package cvut.fel.sit.mojefinance.user.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cvut.fel.sit.mojefinance.user.config.UserTestConfiguration;
import cvut.fel.sit.mojefinance.user.data.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = UserTestConfiguration.class,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create"
        }
)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class RegistrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @WithMockUser(username = "testuser")
    void registerUser_ShouldSaveNewUserToDatabase() throws Exception {
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("username", "new_user_123");
        registrationRequest.put("email", "newuser@example.com");
        registrationRequest.put("firstName", "Jane");
        registrationRequest.put("lastName", "Doe");

        mockMvc.perform(post("/registration")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        long userCount = userJpaRepository.count();
        assertThat(userCount).isEqualTo(1L);
    }
}