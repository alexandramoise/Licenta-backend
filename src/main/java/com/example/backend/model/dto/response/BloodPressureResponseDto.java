package com.example.backend.model.dto.response;

import com.example.backend.model.entity.BloodPressureType;
import lombok.Data;

import java.util.Date;
@Data
public class BloodPressureResponseDto {
    private Long id;
    private Integer systolic;
    private Integer diastolic;
    private Integer pulse;
    private Date date;
    private Boolean isEditable;
    private String patientEmailAddress;
    private BloodPressureType bloodPressureType;
}
