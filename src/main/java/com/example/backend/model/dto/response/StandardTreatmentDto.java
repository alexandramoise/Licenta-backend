package com.example.backend.model.dto.response;

import lombok.Data;

@Data
public class StandardTreatmentDto {
    private String medicalConditionName;
    private String medicineName;
    private Integer doses;
}
