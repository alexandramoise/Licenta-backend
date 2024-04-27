package com.example.backend.model.dto.request;

import lombok.Data;

@Data
public class DoctorRequestDto {
    private String fullName;
    private String email;
    private String password;
}
