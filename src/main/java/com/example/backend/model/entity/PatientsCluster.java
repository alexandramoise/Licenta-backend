package com.example.backend.model.entity;

import com.example.backend.model.dto.response.BloodPressureResponseDto;
import com.example.backend.model.entity.table.BloodPressure;
import com.example.backend.model.entity.table.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientsCluster {
    private int clusterLabel;
    private List<String> patients;
    private List<List<BloodPressureResponseDto>> bloodPressuresInTheGroup;
}
