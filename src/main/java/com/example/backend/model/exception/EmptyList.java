package com.example.backend.model.exception;

public class EmptyList extends RuntimeException {
    public EmptyList(String message) {
        super(message);
    }
}
