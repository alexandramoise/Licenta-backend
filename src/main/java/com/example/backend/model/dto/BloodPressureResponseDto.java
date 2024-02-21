package com.example.backend.model.dto;

import com.example.backend.model.entity.BloodPressureType;
import lombok.Data;

import java.util.Date;
@Data
public class BloodPressureResponseDto {
    private Integer systolic;
    private Integer diastolic;
    private Integer pulse;
    private Date date;
    private Boolean isEditable;
    private String patientEmailAddress;
    private BloodPressureType bloodPressureType;
}
