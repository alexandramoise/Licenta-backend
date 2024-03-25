package com.example.backend.service;

import com.example.backend.model.dto.BloodPressureResponseDto;
import com.example.backend.model.dto.PatientRequestDto;
import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.model.dto.PatientUpdateDto;
import com.example.backend.model.entity.BloodPressureType;
import com.example.backend.model.entity.MedicalCondition;
import com.example.backend.model.entity.Patient;
import com.example.backend.model.exception.ObjectNotFound;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface PatientService {
    PatientResponseDto createAccount(String email, Long doctorId);
    PatientResponseDto updateAccount(PatientUpdateDto patientUpdateDto);
    List<PatientResponseDto> getAllPatients(String doctorEmail);
    PatientResponseDto getPatientById(Long id);

    Integer calculateAge(Date dateOfBirth);

    List<String> getPatientsMedicalConditions(Long id);
}
