package com.example.backend.service;

import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.response.StatisticsForListOfPatientsDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public interface StatisticsForListOfPatientsService {
    List<Double> getPercentageForEachType(List<PatientResponseDto> patients, String fromDate, String toDate);
    List<Integer> getNumberOfEachGender(List<PatientResponseDto> patients);
    List<Integer> getNumberOfWomenWithEachType(List<PatientResponseDto> patients, String fromDate, String toDate);
    List<Integer> getNumberOfMenWithEachType(List<PatientResponseDto> patients, String fromDate, String toDate);
    List<?> getPatientsWithExtremeBloodPressures(List<PatientResponseDto> patients, String fromDate, String toDate);
    List<?> getPatientWithMostVisits(List<PatientResponseDto> patients, String fromDate, String toDate);
    StatisticsForListOfPatientsDto generateStatistics(List<PatientResponseDto> patients, String fromDate, String toDate);
}
