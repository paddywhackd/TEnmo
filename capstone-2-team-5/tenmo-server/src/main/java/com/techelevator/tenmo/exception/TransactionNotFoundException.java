package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Transaction not found.")
public class TransactionNotFoundException extends Exception{
    public TransactionNotFoundException(){
        super("Transaction not found");
    }
}
