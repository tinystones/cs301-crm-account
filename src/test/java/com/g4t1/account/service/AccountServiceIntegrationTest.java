package com.g4t1.account.service;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import com.g4t1.account.config.TestSecurityConfig;
import com.g4t1.account.entity.Account;
import com.g4t1.account.repository.AccountRepository;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestSecurityConfig.class)
public class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private Account validAccount;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        validAccount = new Account(null, "UU12Hopper793", "Savings", "Active", LocalDate.now(),
                new BigDecimal("22773.34"), "SGD", "UBS-01");
    }

    private Account createTestAccount(String clientId) {
        return new Account(null, clientId, "Savings", "Active", LocalDate.now(),
                new BigDecimal("22773.34"), "SGD", "UBS-01");
    }

    @Nested
    @DisplayName("Create Account Service Tests")
    class CreateAccountServiceTests {

        @Test
        @DisplayName("Should create account successfully with valid data")
        void createAccount_ValidData_ReturnsAccount() {
            // When
            Account created = accountService.createAccount(validAccount);

            // Then
            assertNotNull(created);
            assertNotNull(created.getId(), "Account ID should be generated");
            assertEquals("UU12Hopper793", created.getClientId());
            assertEquals("Savings", created.getAccountType());
            assertEquals("Active", created.getAccountStatus());
            assertEquals(LocalDate.now(), created.getOpeningDate());
            assertEquals(new BigDecimal("22773.34"), created.getInitialDeposit());
            assertEquals("SGD", created.getCurrency());
            assertEquals("UBS-01", created.getBranchId());
            assertTrue(accountRepository.existsById(created.getId()));
        }
    }

    @Nested
    @DisplayName("Get Account Service Tests")
    class GetAccountServiceTests {

        private String existingAccountId;

        @BeforeEach
        void setUpAccount() {
            Account savedAccount = accountService.createAccount(validAccount);
            existingAccountId = savedAccount.getId();
        }

        @Test
        @DisplayName("Should retrieve existing account")
        void getAccount_ExistingId_ReturnsAccount() {
            // When
            Account retrieved = accountService.getAccount(existingAccountId);

            // Then
            assertNotNull(retrieved, "Account should be retrieved");
            assertEquals(existingAccountId, retrieved.getId());
            assertEquals("UU12Hopper793", retrieved.getClientId());
            assertEquals("Savings", retrieved.getAccountType());
            assertEquals("Active", retrieved.getAccountStatus());
            assertEquals(LocalDate.now(), retrieved.getOpeningDate());
            assertEquals(new BigDecimal("22773.34"), retrieved.getInitialDeposit());
            assertEquals("SGD", retrieved.getCurrency());
            assertEquals("UBS-01", retrieved.getBranchId());
        }

        @Test
        @DisplayName("Should reflect database changes in retrieved account")
        void getAccount_AfterDatabaseUpdate_ReturnsUpdatedData() {
            // Given - directly modify database
            Account accountToUpdate = accountRepository.findById(existingAccountId).get();
            accountToUpdate.setAccountStatus("Frozen");
            accountRepository.save(accountToUpdate);

            // When
            Account retrieved = accountService.getAccount(existingAccountId);

            // Then
            assertEquals("Frozen", retrieved.getAccountStatus(), "Should reflect database changes");
        }
    }

    @Nested
    @DisplayName("Delete Account Service Tests")
    class DeleteAccountServiceTests {

        private String existingAccountId;

        @BeforeEach
        void setUpAccount() {
            Account savedAccount = accountService.createAccount(validAccount);
            existingAccountId = savedAccount.getId();
        }

        @Test
        @DisplayName("Should delete only target account when multiple accounts exist")
        void deleteAccount_MultipleAccounts_DeletesOnlyTarget() {
            // Given - create additional account
            Account anotherAccount =
                    accountService.createAccount(createTestAccount("UU12AnotherClient"));
            String anotherAccountId = anotherAccount.getId();

            assertTrue(accountRepository.existsById(existingAccountId));
            assertTrue(accountRepository.existsById(anotherAccountId));
            assertEquals(2, accountRepository.count());

            // When
            boolean result = accountService.deleteAccount(existingAccountId);

            // Then
            assertTrue(result);
            assertFalse(accountRepository.existsById(existingAccountId),
                    "Target account should be deleted");
            assertTrue(accountRepository.existsById(anotherAccountId),
                    "Other account should remain");
            assertEquals(1, accountRepository.count(), "Should have 1 account remaining");
        }
    }
}
