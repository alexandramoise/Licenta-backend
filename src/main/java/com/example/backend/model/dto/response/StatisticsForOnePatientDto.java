package com.example.backend.model.dto.response;

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
    private List<String> conditions;
    private Integer conditionsFavoringHypertension;
    private Integer conditionsFavoringHypotension;
    private String favoringCondition;
}
