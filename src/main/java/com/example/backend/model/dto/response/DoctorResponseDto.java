package com.example.backend.model.dto.response;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.Patient;
import lombok.Data;

import java.util.List;
@Data
public class DoctorResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private List<PatientResponseDto> patients;
    private List<AppointmentResponseDto> appointments;
}
