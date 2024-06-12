package com.example.backend.model.dto.response;

import com.example.backend.model.entity.table.MedicalCondition;
import lombok.Data;

import java.util.List;

@Data
public class StatisticsForOnePatientDto {
    private Double averageSystolic;
    private Double averageDiastolic;
    private Double averagePulse;
    private BloodPressureResponseDto maxBp;
    private BloodPressureResponseDto minBp;
    private Integer numberOfVisits;
    private List<MedicalCondition> conditions;
    private Integer conditionsFavoringHypertension;
    private Integer conditionsFavoringHypotension;
    private String favoringCondition;
}
