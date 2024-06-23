package com.example.backend.model.dto.response;

import lombok.Data;

import java.util.List;
@Data
public class DoctorResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private Boolean acceptedTermsAndConditions;
    private Boolean sendNotifications;
//  private List<PatientResponseDto> patients;
//  private List<AppointmentResponseDto> appointments;
}
