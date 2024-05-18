package com.example.backend.service;

import com.example.backend.model.dto.MedicalConditionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MedicalConditionService {
    List<MedicalConditionDto> getPatientCurrentMedicalConditions(String patientEmail);
    List<MedicalConditionDto> getPatientAllMedicalConditions(String patientEmail);
}
