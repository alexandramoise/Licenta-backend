package com.example.backend.model.dto;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.BloodPressure;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Gender;

import java.util.Date;
import java.util.List;

public class PatientRequestDto {
    private String fullName;
    private String email;
    private String password;
    private Boolean firstLoginEver;
    private Date dateOfBirth;
    private Gender gender;
    private Doctor doctor;
    private List<Appointment> appointments;
    private List<BloodPressure> bloodPressures;
}
