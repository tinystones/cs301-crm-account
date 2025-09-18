package com.g4t1.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.g4t1.account.entity.Account;
import com.g4t1.account.exceptions.AccountNotFoundException;
import com.g4t1.account.exceptions.InvalidAccountSouceDataException;
import com.g4t1.account.repository.AccountRepository;
import com.g4t1.account.service.impl.AccountServiceImpl;

@ExtendWith(MockitoExtension.class)
class AccountApplicationTests {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountServiceImpl service;

    private Account arrangeGoodSource() {
        Account goodSource = new Account(null, "UU12Hopper793", "Savings", "Active",
                LocalDate.now(), new BigDecimal("22773.34"), "SGD", "UBS-01");
        return goodSource;
    }

    private Account arrangeBadSource() {
        Account goodSource = new Account(null, "UU12Hopper793", null, "Active", LocalDate.now(),
                new BigDecimal("22773.34"), "  ", null);
        return goodSource;
    }

    // TODO: write tests for validateAccount()

    @Nested
    class ValidateSourceDataTests {

        /* Arrange */
        Account goodSource;
        Account badSource;

        @BeforeEach
        void arrangeSources() {
            goodSource = arrangeGoodSource();
            badSource = arrangeBadSource();
        }

        @Test
        void validateSourceData_givenGoodSource_returnTrue() {
            /* Act & Assert */
            boolean result = service.validateSourceData(goodSource);
            assertTrue(result);
        }

        @Test
        void validateSourceData_givenBadSource_returnFalse() {
            /* Act & Assert */
            boolean result = service.validateSourceData(badSource);
            assertFalse(result);
        }

        @Test
        void validateSourceData_givenNull_returnFalse() {
            /* Act & Assert */
            boolean result = service.validateSourceData(null);
            assertFalse(result);
        }

        @Test
        void validateSourceData_givenSourceWithID_returnFalse() {
            /* Act & Assert */
            goodSource.setId("existing-id");
            boolean result = service.validateSourceData(goodSource);
            assertFalse(result);
        }
    }

    @Nested
    class CreateAccountTests {

        /* Arrange */
        Account goodSource;
        Account badSource;
        InvalidAccountSouceDataException ex;

        @BeforeEach
        void arrangeSources() {
            goodSource = arrangeGoodSource();
            badSource = arrangeBadSource();
            ex = null;
        }

        @Test
        void createAccount_givenNull_throwsInvalidAccountSourceDataException() {
            /* Act & Assert */
            ex = assertThrows(InvalidAccountSouceDataException.class,
                    () -> service.createAccount(null));
            assertEquals(ex.getMessage(), "invalid account source data, please check fields");
        }

        @Test
        void createAccount_givenAccountWithID_throwsInvalidAccountSourceDataException() {
            /* Arrange */
            goodSource.setId("exisiting-id");
            
            /* Act & Assert */
            ex = assertThrows(InvalidAccountSouceDataException.class,
                    () -> service.createAccount(goodSource));
            assertEquals(ex.getMessage(), "invalid account source data, please check fields");
        }

        @Test
        void createAccount_givenBadSource_throwsInvalidAccountSourceDataException() {
            /* Act & Assert */
            ex = assertThrows(InvalidAccountSouceDataException.class,
                    () -> service.createAccount(badSource));
            assertEquals(ex.getMessage(), "invalid account source data, please check fields");
        }

        @Test
        void createAccount_givenGoodSource_savesSuccessfully() {
            /* Arrange */
            when(repository.save(any(Account.class))).thenReturn(goodSource);

            /* Act & Assert */
            Account result = service.createAccount(goodSource);
            assertEquals(Account.class, result.getClass());
            assertEquals("UU12Hopper793", result.getClientId());
            assertEquals(BigDecimal.valueOf(22773.34), result.getInitialDeposit());
            assertTrue(result.getId() instanceof String);
        }
    }

    @Nested
    class GetAccountTests {
        /* Arrange */
        Account targetAccount;
        String targetId;
        RuntimeException ex;

        @BeforeEach
        void arrangeRepo() {
            Account goodSource = arrangeGoodSource();
            ex = null;

            when(repository.save(any(Account.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            targetAccount = service.createAccount(goodSource);
            targetId = targetAccount.getId();
        }

        @Test
        void getAccount_givenNull_throwIllegalArgumentException() {
            /* Act & Assert */
            ex = assertThrows(IllegalArgumentException.class, () -> service.getAccount(null));
            assertEquals(ex.getMessage(), "account id must not be blank");
        }

        @Test
        void getAccount_givenBlankId_throwIllegalArgumentException() {
            /* Act & Assert */
            ex = assertThrows(IllegalArgumentException.class, () -> service.getAccount("   "));
            assertEquals(ex.getMessage(), "account id must not be blank");
        }

        @Test
        void getAccount_givenRandomTargetId_throwAccountNotFoundException() {
            /* Act & Assert */
            ex = assertThrows(AccountNotFoundException.class,
                    () -> service.getAccount("1-very-challenging-brick-wall"));
            assertEquals(ex.getMessage(), "account not found");
        }

        @Test
        void getAccount_givenExisitingTargetId_returnsAccount() {

            /* Arrage */
            when(repository.existsById(targetId)).thenReturn(true);
            when(repository.findById(targetId)).thenReturn(java.util.Optional.of(targetAccount));

            /* Act & Assert */
            Account result = service.getAccount(targetId);
            assertNotNull(result); // check if account is found
            assertEquals(targetId, result.getId()); // check if same UUID
            assertEquals(Account.class, result.getClass());
            assertEquals("UU12Hopper793", result.getClientId());
            assertEquals(BigDecimal.valueOf(22773.34), result.getInitialDeposit());
            assertEquals("SGD", result.getCurrency());
        }
    }

    @Nested
    class DeleteAccountTests {

        /* Arrange */
        Account targetAccount;
        String targetId;
        RuntimeException ex;

        @BeforeEach
        void arrangeRepo() {
            Account goodSource = arrangeGoodSource();
            ex = null;

            when(repository.save(any(Account.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            targetAccount = service.createAccount(goodSource);
            targetId = targetAccount.getId();
        }

        @Test
        void deleteAccount_givenNull_throwIllegalArgumentException() {
            /* Act & Assert */
            ex = assertThrows(IllegalArgumentException.class, () -> service.deleteAccount(null));
            assertEquals(ex.getMessage(), "account id must not be blank");
        }

        @Test
        void deleteAccount_givenBlank_throwIllegalArgumentException() {
            /* Act & Assert */
            ex = assertThrows(IllegalArgumentException.class, () -> service.deleteAccount("   "));
            assertEquals(ex.getMessage(), "account id must not be blank");
        }

        @Test
        void deleteAccount_givenRandomTargetId_throwAccountNotFoundException() {
            /* Act & Assert */
            ex = assertThrows(AccountNotFoundException.class,
                    () -> service.deleteAccount("1-very-challenging-brick-wall"));
            assertEquals(ex.getMessage(), "account not found");
        }

        @Test
        void deleteAccount_givenTargetId_returnTrue() {
            /* Arrage */
            when(repository.existsById(targetId)).thenReturn(true);

            /* Act & Assert */
            assertTrue(service.deleteAccount(targetId));
        }
    }
}
