package com.example.backend.model.dto.response;

import lombok.Data;

@Data
public class StatisticsForListOfPatientsDto {
    private Integer totalNumberOfPatients;
    private Double hypertensivePercentage;
    private Double normalPercentage;
    private Double hypotensivePercentage;
    private Integer women;
    private Integer men;
    private Integer other;
    private Integer womenWithHypertension;
    private Integer womenWithHypotension;
    private Integer womenWithNormal;
    private Integer menWithHypertension;
    private Integer menWithHypotension;
    private Integer menWithNormal;
    private String patientWithMaxBloodPressure;
    private String patientWithMinBloodPressure;
    private BloodPressureResponseDto maxBp;
    private BloodPressureResponseDto minBp;
    private String patientWithMostVisits;
    private Integer maxVisits;
}
