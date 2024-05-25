package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MedicineService {
    List<String> getMedicinesForMedicalCondition(String medicalCondition);
}
