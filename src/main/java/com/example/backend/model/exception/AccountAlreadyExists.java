package com.example.backend.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AccountAlreadyExists extends RuntimeException {
    public AccountAlreadyExists(String message) {
        super(message);
    }
}
