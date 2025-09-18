package com.g4t1.account.service;

import com.g4t1.account.entity.Account;

public interface AccountService {

    boolean validateSourceData(Account source);

    boolean validateAccount(String targetId);

    Account createAccount(Account accountData);

    Account getAccount(String accountId);

    boolean deleteAccount(String accountId);
}
