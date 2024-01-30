package com.example.backend.model.exception;

public class InvalidAccountType extends RuntimeException {
    public InvalidAccountType(String message) {
        super(message);
    }
}
