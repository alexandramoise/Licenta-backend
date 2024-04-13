package com.example.backend.service;

import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.update.PatientUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface PatientService {
    PatientResponseDto createAccount(String email, String doctorEmail);
    PatientResponseDto updateAccount(PatientUpdateDto patientUpdateDto, String email);
    List<PatientResponseDto> getAllPatients(String doctorEmail);

    Page<PatientResponseDto> getAllPagedPatients(String doctorEmail, Pageable pageable);
    PatientResponseDto getPatientById(Long id);

    PatientResponseDto getPatientByEmail(String email);

    Integer calculateAge(Date dateOfBirth);

    List<String> getPatientsMedicalConditions(Long id);

    void setHypoOrHypertension(Long id);
}
