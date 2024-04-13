package com.example.backend.model.dto.request;

import lombok.Data;

@Data
public class TreatmentRequestDto {
    private Long patientId;
    private String medicalConditionName;
    private String medicineName;
    private Integer doses;
}
