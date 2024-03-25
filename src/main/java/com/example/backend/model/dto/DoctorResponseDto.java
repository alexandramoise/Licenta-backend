package com.example.backend.model.dto;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.Patient;
import lombok.Data;

import java.util.List;
@Data
public class DoctorResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private List<Patient> patients;
    private List<Appointment> appointments;
}
