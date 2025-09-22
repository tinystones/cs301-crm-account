package com.g4t1.account.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.g4t1.account.entity.Account;
import com.g4t1.account.service.impl.AccountServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


@RestController
@Validated
public class AccountController {

    private final AccountServiceImpl accountService;

    public AccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/api/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody @Valid Account account) {
        Account newlyCreated = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(newlyCreated);
    }

    @GetMapping("/api/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable @NotBlank String id) {
        Account target = accountService.getAccount(id);
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @DeleteMapping("/api/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable @NotBlank String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
