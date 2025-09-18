package com.g4t1.account.exceptions;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(){
        super("account not found");
    }
}
