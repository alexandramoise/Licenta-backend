package com.example.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

public enum Gender {
    Masculine,
    Feminine,
    Other;

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
