package com.example.backend.model.exception;

public class CantBeEdited extends RuntimeException {
    public CantBeEdited(String message) {
        super(message);
    }
}
