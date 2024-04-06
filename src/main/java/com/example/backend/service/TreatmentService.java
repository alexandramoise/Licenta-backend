package com.example.backend.service;

import com.example.backend.model.dto.StandardTreatmentDto;
import com.example.backend.model.dto.TreatmentRequestDto;
import com.example.backend.model.dto.TreatmentResponseDto;
import com.example.backend.model.entity.BloodPressureType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TreatmentService {
    TreatmentResponseDto addTreatment(TreatmentRequestDto treatmentRequestDto);
    List<TreatmentResponseDto> getPatientTreatments(String patientEmail, String medicalConditionName);

    Page<TreatmentResponseDto> getPagedTreatments(String patientEmail, String medicalConditionName, Pageable pageable);
    StandardTreatmentDto standardTreatmentScheme(BloodPressureType bloodPressureType);

    void setStandardTreatmentScheme(Long id, BloodPressureType bloodPressureType);
}
