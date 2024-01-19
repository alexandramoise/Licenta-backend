package com.example.backend.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AccountNotFound extends RuntimeException {
    public AccountNotFound(String message) {
        super(message);
    }
}
