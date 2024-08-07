package com.example.backend.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidAccountType extends RuntimeException {
    public InvalidAccountType(String message) {
        super(message);
    }
}
