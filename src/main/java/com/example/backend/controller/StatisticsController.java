package com.example.backend.controller;

import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.response.StatisticsForListOfPatientsDto;
import com.example.backend.model.dto.response.StatisticsForOnePatientDto;
import com.example.backend.service.StatisticsForListOfPatientsService;
import com.example.backend.service.StatisticsForOnePatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsForListOfPatientsService statisticsForListOfPatientsService;
    private final StatisticsForOnePatientService statisticsForOnePatientService;

    public StatisticsController(StatisticsForListOfPatientsService statisticsForListOfPatientsService, StatisticsForOnePatientService statisticsForOnePatientService) {
        this.statisticsForListOfPatientsService = statisticsForListOfPatientsService;
        this.statisticsForOnePatientService = statisticsForOnePatientService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getStatisticsForList(@RequestParam("fromDate") String fromDate,
                                                  @RequestParam("toDate") String toDate,
                                                  @RequestBody List<PatientResponseDto> patients) {
        if(patients.size() > 1) {
            StatisticsForListOfPatientsDto result = statisticsForListOfPatientsService.generateStatistics(patients, fromDate, toDate);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity("EMPTY LIST", HttpStatus.OK);
    }

    @GetMapping("/one")
    public ResponseEntity<?> getStatisticsForOnePatient(@RequestParam("fromDate") String fromDate,
                                                        @RequestParam("toDate") String toDate,
                                                        @RequestParam("email") String patientEmail) {
        StatisticsForOnePatientDto result = statisticsForOnePatientService.generateStatistics(patientEmail, fromDate, toDate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
