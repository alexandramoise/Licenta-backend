package com.example.backend.utils;

import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.AccountNotFound;
import com.example.backend.model.exception.InvalidAccountType;
import com.example.backend.model.exception.InvalidCredentials;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AccountAlreadyExists.class)
    public ResponseEntity<AccountAlreadyExists> accountAlreadyExists(AccountAlreadyExists accountAlreadyExists) {
        return new ResponseEntity<>(accountAlreadyExists, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = AccountNotFound.class)
    public ResponseEntity<AccountNotFound> accountNotFound(AccountNotFound accountNotFound) {
        return new ResponseEntity<>(accountNotFound, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InvalidCredentials.class)
    public ResponseEntity<InvalidCredentials> invalidCredentials(InvalidCredentials invalidCredentials) {
        return new ResponseEntity<>(invalidCredentials, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InvalidAccountType.class)
    public ResponseEntity<InvalidAccountType> invalidCredentials(InvalidAccountType invalidAccountType) {
        return new ResponseEntity<>(invalidAccountType, HttpStatus.BAD_REQUEST);
    }
}
