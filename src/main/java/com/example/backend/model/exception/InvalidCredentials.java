package com.example.backend.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidCredentials extends RuntimeException {
    public InvalidCredentials(String message) {
        super(message);
    }
}
