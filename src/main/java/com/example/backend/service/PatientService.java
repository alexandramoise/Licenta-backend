package com.example.backend.service;

import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.model.entity.BloodPressureType;
import com.example.backend.model.entity.MedicalCondition;
import com.example.backend.model.exception.ObjectNotFound;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface PatientService {
    List<PatientResponseDto> getAllPatients(String doctorEmail);
    PatientResponseDto getPatientById(Long id);

    Integer calculateAge(Date dateOfBirth);

    List<String> getPatientsMedicalConditions(Long id);
}
