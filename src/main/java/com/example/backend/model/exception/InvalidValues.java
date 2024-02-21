package com.example.backend.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidValues extends RuntimeException {
    public InvalidValues(String message) {
        super(message);
    }
}
