package com.example.backend.model.dto;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.BloodPressure;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Gender;

import java.util.Date;
import java.util.List;

public class PatientResponseDto {
    private String fullName;
    private String email;
    private Boolean firstLoginEver;
    private Integer age;
    private Gender gender;
    private Integer doctorId;
    private List<Appointment> appointments;
    private List<BloodPressure> bloodPressures;

    private String bloodPressureTendency;
}
