package com.example.backend.service;

import com.example.backend.model.dto.response.StandardTreatmentDto;
import com.example.backend.model.dto.request.TreatmentRequestDto;
import com.example.backend.model.dto.response.TreatmentResponseDto;
import com.example.backend.model.dto.update.TreatmentUpdateDto;
import com.example.backend.model.entity.BloodPressureType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TreatmentService {
    TreatmentResponseDto addTreatment(TreatmentRequestDto treatmentRequestDto);
    TreatmentResponseDto updateTreatment(Long id, TreatmentUpdateDto treatmentUpdateDto);
    TreatmentResponseDto getTreatmentById(Long id);
    TreatmentResponseDto markAsEnded(Long id);
    List<TreatmentResponseDto> getPatientTreatments(String patientEmail, String medicalConditionName);

    List<TreatmentResponseDto> getTreatmentsByTime(String patientEmail, String medicalConditionName, String fromDate, String toDate);
    Page<TreatmentResponseDto> getPagedTreatments(String patientEmail, String medicalConditionName, Pageable pageable);
    StandardTreatmentDto standardTreatmentScheme(BloodPressureType bloodPressureType);

    void setStandardTreatmentScheme(Long id, BloodPressureType bloodPressureType);
}
