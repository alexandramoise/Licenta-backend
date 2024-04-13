package com.example.backend.model.dto.request;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Gender;
import lombok.Data;

import java.util.Date;

@Data
public class PatientRequestDto {
    private String fullName;
    private String email;
    private String password;
    private Boolean firstLoginEver;
    private Date dateOfBirth;
    private Gender gender;
}
