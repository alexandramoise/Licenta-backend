package com.example.backend.model.dto;

import com.example.backend.model.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class PatientResponseDto {
    private String fullName;
    private String email;
    private Boolean firstLoginEver;
    private Integer age;
    private Gender gender;
    private String doctorEmailAddress;
    private List<AppointmentResponseDto> appointments;
    private List<BloodPressureResponseDto> bloodPressures;
}
