package com.example.backend.service;

import com.example.backend.model.dto.TreatmentRequestDto;
import com.example.backend.model.dto.TreatmentResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TreatmentService {
    TreatmentResponseDto addTreatment(TreatmentRequestDto treatmentRequestDto);
    List<TreatmentResponseDto> getPatientTreatments(String patientEmail, String medicalConditionName);
}
