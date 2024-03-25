package com.example.backend.model.dto;

import com.example.backend.model.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class PatientResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private Boolean firstLoginEver;
    private Integer age;
    private Gender gender;
    private String doctorEmailAddress;
    private BloodPressureType tendency;
    private List<AppointmentResponseDto> appointments;
    private List<BloodPressureResponseDto> bloodPressures;
    private List<MedicalConditionRequestDto> medicalConditions;
}
