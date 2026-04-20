package cvut.fel.sit.mojefinance.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.config.BankTestConfiguration;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionJpaRepository;
import cvut.fel.sit.mojefinance.openapi.model.BankConnection;
import cvut.fel.sit.mojefinance.openapi.model.ConnectBankRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankTestConfiguration.class,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create"
        })
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class BankConnectionIntegrationTest {

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
    private BankConnectionJpaRepository bankConnectionJpaRepository;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    @WithMockUser(username = "testuser")
    void connectBank_ShouldSaveToDatabase() throws Exception {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setBankName("KB");
        bankConnection.setClientRegistrationId("kb");

        ConnectBankRequest request = new ConnectBankRequest();
        request.setBankConnection(bankConnection);

        mockMvc.perform(post("/banks/connect")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .param("code", "auth-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        long count = bankConnectionJpaRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getConnectedBanks_ShouldReturnSavedBanks() throws Exception {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setBankName("AirBank");
        bankConnection.setClientRegistrationId("air-bank");
        ConnectBankRequest request = new ConnectBankRequest();
        request.setBankConnection(bankConnection);

        mockMvc.perform(post("/banks/connect")
                        .with(csrf())
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/banks")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectedBanks", hasSize(1)))
                .andExpect(jsonPath("$.connectedBanks[0].bankName", is("AirBank")))
                .andExpect(jsonPath("$.connectedBanks[0].clientRegistrationId", is("air-bank")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void disconnectBank_ShouldRemoveBankFromDatabase() throws Exception {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setBankName("CSOB");
        bankConnection.setClientRegistrationId("csob");
        ConnectBankRequest request = new ConnectBankRequest();
        request.setBankConnection(bankConnection);

        mockMvc.perform(post("/banks/connect")
                        .with(csrf())
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(bankConnectionJpaRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/banks/disconnect/csob")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(bankConnectionJpaRepository.count()).isEqualTo(0);
    }

    @Test
    @WithMockUser(username = "testuser")
    void connectBank_WhenBankInfoIsMissing_ShouldNotSaveToDatabase() {
        ConnectBankRequest request = new ConnectBankRequest();
        try {
            mockMvc.perform(post("/banks/connect")
                    .with(csrf())
                    .header("Authorization", "Bearer test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        } catch (Exception ignored) {
        }
        assertThat(bankConnectionJpaRepository.count()).isEqualTo(0);
    }
}