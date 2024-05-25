package com.example.backend.controller;

import com.example.backend.model.repo.MedicineRepo;
import com.example.backend.service.MedicineService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }
    @GetMapping
    public ResponseEntity<List<String>> getMedicinesForMedicalCondition(@RequestParam(name = "medicalCondition") String medicalCondition) {
        List<String> result = medicineService.getMedicinesForMedicalCondition(medicalCondition);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
