package com.g4t1.account.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g4t1.account.config.TestSecurityConfig;
import com.g4t1.account.entity.Account;
import com.g4t1.account.repository.AccountRepository;
import com.g4t1.account.service.impl.AccountServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestSecurityConfig.class)
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountServiceImpl accountService;

    private Account validAccount;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        validAccount = new Account(null, "UU12Hopper793", "Savings", "Active", LocalDate.now(),
                new BigDecimal("22773.34"), "SGD", "UBS-01");
    }

    @Nested
    @DisplayName("Create Account Tests")
    class CreateAccountTests {

        @Test
        @DisplayName("Should return 400 when no JSON body is provided")
        void createAccount_NoJsonBody_Returns400() throws Exception {
            mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 201 when valid JSON is provided")
        void createAccount_ValidJson_Returns201() throws Exception {
            mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAccount)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.clientId", is("UU12Hopper793")))
                    .andExpect(jsonPath("$.accountType", is("Savings")))
                    .andExpect(jsonPath("$.accountStatus", is("Active")))
                    .andExpect(jsonPath("$.initialDeposit", is(22773.34)))
                    .andExpect(jsonPath("$.currency", is("SGD")))
                    .andExpect(jsonPath("$.branchId", is("UBS-01")));
        }
    }

    @Nested
    @DisplayName("Get Account Tests")
    class GetAccountTests {

        @Test
        @DisplayName("Should return 404 when account does not exist")
        void getAccount_NonExistingId_Returns404() throws Exception {
            String nonExistingId = UUID.randomUUID().toString();

            mockMvc.perform(get("/api/accounts/{id}", nonExistingId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 200 when account exist")
        void getAccount_ExistingId_Returns200() throws Exception {
            Account savedAccount = accountService.createAccount(validAccount);

            mockMvc.perform(get("/api/accounts/{id}", savedAccount.getId()))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.clientId", is("UU12Hopper793")))
                    .andExpect(jsonPath("$.accountType", is("Savings")))
                    .andExpect(jsonPath("$.accountStatus", is("Active")))
                    .andExpect(jsonPath("$.initialDeposit", is(22773.34)))
                    .andExpect(jsonPath("$.currency", is("SGD")))
                    .andExpect(jsonPath("$.branchId", is("UBS-01")));
        }
    }

    @Nested
    @DisplayName("Delete Account Tests")
    class DeleteAccountTests {

        @Test
        @DisplayName("Should return 400 when ID contains only whitespace")
        void deleteAccount_WhitespaceId_Returns400() throws Exception {
            mockMvc.perform(delete("/api/accounts/{id}", "%20%20%20"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when account does not exist")
        void deleteAccount_NonExistingId_Returns404() throws Exception {
            String nonExistingId = UUID.randomUUID().toString();

            mockMvc.perform(delete("/api/accounts/{id}", nonExistingId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 204 when deletion is successful")
        void deleteAccount_ExistingId_Returns204() throws Exception {
            Account savedAccount = accountService.createAccount(validAccount);

            mockMvc.perform(delete("/api/accounts/{id}", savedAccount.getId()))
                    .andExpect(status().isNoContent());

            // verify if account is deleted from repository
            mockMvc.perform(get("/api/accounts/{id}", savedAccount.getId()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Error Handling Repeatability Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle validation errors consistently")
        void createAccount_ValidationErrors_Returns400Consistently() throws Exception {
            // test multiple validation scenarios
            Account[] invalidAccounts = {
                    new Account(null, null, "Savings", "Active", LocalDate.now(),
                            new BigDecimal("10000"), "SGD", "UBS-01"), // null clientId
                    new Account(null, "UU12Test", null, "Active", LocalDate.now(),
                            new BigDecimal("10000"), "SGD", "UBS-01"), // null accountType
                    new Account(null, "UU12Test", "Savings", null, LocalDate.now(),
                            new BigDecimal("10000"), "SGD", "UBS-01"), // null status
                    new Account(null, "UU12Test", "Savings", "Active", null,
                            new BigDecimal("10000"), "SGD", "UBS-01"), // null opening date
                    new Account(null, "UU12Test", "Savings", "Active", LocalDate.now(), null, "SGD",
                            "UBS-01"), // null initial deposit
                    new Account(null, "UU12Test", "Savings", "Active", LocalDate.now(),
                            new BigDecimal("10000"), null, "UBS-01"), // null currency
                    new Account(null, "UU12Test", "Savings", "Active", LocalDate.now(),
                            new BigDecimal("10000"), "SGD", null) // null branchId
            };

            for (Account invalidAccount : invalidAccounts) {
                mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAccount)))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
