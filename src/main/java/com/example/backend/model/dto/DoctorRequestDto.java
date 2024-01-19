package com.example.backend.model.dto;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.Patient;
import lombok.Data;

import java.util.List;

@Data
public class DoctorRequestDto {
    private String fullName;
    private String email;
    private String password;
    private List<Patient> patients;
    private List<Appointment> appointments;
}
