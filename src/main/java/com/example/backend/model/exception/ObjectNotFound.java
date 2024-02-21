package com.example.backend.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ObjectNotFound extends RuntimeException {
    public ObjectNotFound(String message) {
        super(message);
    }
}
