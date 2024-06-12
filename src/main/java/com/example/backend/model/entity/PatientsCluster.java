package com.example.backend.model.entity;

import com.example.backend.model.dto.response.BloodPressureResponseDto;
import com.example.backend.model.dto.response.TreatmentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientsCluster {
    private int clusterLabel;
    private List<String> patientEmails;
    private List<String> patientNames;
    private List<List<BloodPressureResponseDto>> bloodPressuresInTheGroup;
    private List<List<TreatmentResponseDto>> treatmentsInTheGroup;
}
