package com.bank.bank_rest.integration;

import com.bank.bank_rest.dto.login.LoginRequest;
import com.bank.bank_rest.dto.login.LoginResponse;
import com.bank.bank_rest.model.enums.CardStatus;
import com.bank.bank_rest.repository.CardRepository;
import com.bank.bank_rest.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BankingIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void contextLoads() {
        assert userRepository != null;
        assert cardRepository != null;
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCardServiceMicrocase() throws Exception {
        setup();

        // Test user authentication endpoint
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new LoginRequest("admin", "admin123")
                )))
                .andExpect(status().isOk())
                .andReturn();

        // Test cards endpoint with proper authentication
        mockMvc.perform(get("/api/cards")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCrdSecurityEndpointsProtected() throws Exception {
        setup();

        // Unauthorized requests to protected endpoints should fail
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
                
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testApiDocumentationAccess() throws Exception {
        setup();

        // Swagger UI should be accessible without authentication
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }
}
