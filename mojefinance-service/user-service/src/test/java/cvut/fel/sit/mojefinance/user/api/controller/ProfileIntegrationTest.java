package cvut.fel.sit.mojefinance.user.api.controller;

import cvut.fel.sit.mojefinance.user.config.UserTestConfiguration;
import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class ProfileIntegrationTest {

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
    private UserJpaRepository userJpaRepository;

    @Test
    @WithMockUser(username = "testuser")
    void getProfile_ShouldReturnCurrentUserProfile() throws Exception {
        UserEntity testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        userJpaRepository.save(testUser);

        mockMvc.perform(get("/profile")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username", is("testuser")))
                .andExpect(jsonPath("$.user.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.user.firstName", is("John")))
                .andExpect(jsonPath("$.user.lastName", is("Doe")));
    }
}