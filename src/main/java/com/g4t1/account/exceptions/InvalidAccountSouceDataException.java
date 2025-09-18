package com.g4t1.account.exceptions;

public class InvalidAccountSouceDataException extends IllegalArgumentException{
    public InvalidAccountSouceDataException(){
        super("invalid account source data, please check fields");
    }
    
}
