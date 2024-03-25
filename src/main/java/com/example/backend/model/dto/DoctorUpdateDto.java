package com.example.backend.model.dto;

import lombok.Data;

@Data
public class DoctorUpdateDto {
    private String firstName;
    private String lastName;
    private String email;
}
