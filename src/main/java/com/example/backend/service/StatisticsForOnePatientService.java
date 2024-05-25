package com.example.backend.service;

import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.response.StatisticsForOnePatientDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StatisticsForOnePatientService {
    List<?> getAverageAndExtremeValues(String patientEmail, String fromDate, String toDate);
    Integer getNumberOfVisits(String patientEmail, String fromDate, String toDate);
    List<?> medicalConditionsFavoringEachType(String patientEmail, String fromDate, String toDate);
    StatisticsForOnePatientDto generateStatistics(String patientEmail, String fromDate, String toDate);
}
