package com.g4t1.account.service.impl;

import java.lang.reflect.Field;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.g4t1.account.entity.Account;
import com.g4t1.account.exceptions.AccountNotFoundException;
import com.g4t1.account.exceptions.InvalidAccountSouceDataException;
import com.g4t1.account.repository.AccountRepository;
import com.g4t1.account.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accounts;

    public AccountServiceImpl(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public boolean validateSourceData(Account source) {
        if (source == null) {
            return false;
        }

        try {
            for (Field field : Account.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(source);
                String fieldName = field.getName();

                // id should always be null
                if (fieldName.equals("id") && fieldValue != null) {
                    return false;
                }
                // all non-id fields must be non-null
                if (!fieldName.equals("id") && fieldValue == null) {
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("reflection error during validation", e);
        }
    }

    public boolean validateAccount(String targetId) {
        if (!StringUtils.hasText(targetId)) {
            throw new IllegalArgumentException("account id must not be blank");
        }

        if (!accounts.existsById(targetId)) {
            throw new AccountNotFoundException();
        }

        return true;
    }

    public Account createAccount(Account accountData) {
        if (!validateSourceData(accountData)) {
            throw new InvalidAccountSouceDataException();
        }

        try {
            String id = UUID.randomUUID().toString();
            accountData.setId(id);
            return accounts.save(accountData);
        } catch (Exception e) { // catch any repo runtime error
            throw new RuntimeException("failed to create and save client", e);
        }
    }

    public Account getAccount(String id) {
        validateAccount(id);
        try {
            return accounts.findById(id).get();
        } catch (Exception e) { // catch any repo runtime error
            throw new RuntimeException("failed to retrieve account", e);
        }
    }

    public boolean deleteAccount(String id) {
        validateAccount(id);
        try {
            accounts.deleteById(id);
            return true;
        } catch (Exception e) { // catch any repo runtime error
            throw new RuntimeException("failed to delete account", e);
        }
    }
}
