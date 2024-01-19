package com.example.backend.utils;

import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.AccountNotFound;
import com.example.backend.model.exception.InvalidCredentials;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AccountAlreadyExists.class)
    public ResponseEntity<String> accountAlreadyExists(AccountAlreadyExists accountAlreadyExists) {
        return new ResponseEntity<>(accountAlreadyExists.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = AccountNotFound.class)
    public ResponseEntity<String> accountNotFound(AccountNotFound accountNotFound) {
        return new ResponseEntity<>(accountNotFound.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InvalidCredentials.class)
    public ResponseEntity<String> invalidCredentials(InvalidCredentials invalidCredentials) {
        return new ResponseEntity<>(invalidCredentials.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
