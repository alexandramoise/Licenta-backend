package com.example.backend.model.dto.update;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.entity.Gender;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PatientUpdateDto extends DoctorUpdateDto {
    private Date dateOfBirth;
    private Gender gender;
    private List<MedicalConditionDto> medicalConditions;
}
