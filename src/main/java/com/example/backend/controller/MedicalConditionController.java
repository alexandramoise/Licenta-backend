package com.example.backend.controller;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.service.MedicalConditionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-conditions")
public class MedicalConditionController {
    private final MedicalConditionService medicalConditionService;

    public MedicalConditionController(MedicalConditionService medicalConditionService) {
        this.medicalConditionService = medicalConditionService;
    }

    @GetMapping("/current")
    public ResponseEntity<List<MedicalConditionDto>> getPatientCurrentMedicalConditions(@RequestParam(required = true) String email) {
        List<MedicalConditionDto> result = medicalConditionService.getPatientCurrentMedicalConditions(email);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MedicalConditionDto>> getPatientAllMedicalConditions(@RequestParam(required = true) String email) {
        List<MedicalConditionDto> result = medicalConditionService.getPatientAllMedicalConditions(email);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
