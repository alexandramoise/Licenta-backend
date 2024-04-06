package com.example.backend.service;

import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.model.dto.PatientUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface PatientService {
    PatientResponseDto createAccount(String email, Long doctorId);
    PatientResponseDto updateAccount(PatientUpdateDto patientUpdateDto);
    List<PatientResponseDto> getAllPatients(String doctorEmail);

    Page<PatientResponseDto> getAllPagedPatients(String doctorEmail, Pageable pageable);
    PatientResponseDto getPatientById(Long id);

    Integer calculateAge(Date dateOfBirth);

    List<String> getPatientsMedicalConditions(Long id);

    void setHypoOrHypertension(Long id);
}
