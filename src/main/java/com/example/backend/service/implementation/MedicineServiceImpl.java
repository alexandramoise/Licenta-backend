package com.example.backend.service.implementation;

import com.example.backend.model.entity.table.MedicalCondition;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.MedicalConditionRepo;
import com.example.backend.model.repo.MedicineRepo;
import com.example.backend.service.MedicineService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineServiceImpl implements MedicineService {
    private final MedicalConditionRepo medicalConditionRepo;

    public MedicineServiceImpl(MedicalConditionRepo medicalConditionRepo) {
        this.medicalConditionRepo = medicalConditionRepo;
    }

    @Override
    public List<String> getMedicinesForMedicalCondition(String medicalCondition) {
        MedicalCondition medCond = medicalConditionRepo.findByName(medicalCondition).orElseThrow(() -> new ObjectNotFound("Medical condition not found"));
        List<String> result = medCond.getMedicines()
                .stream()
                .map(m -> {
                    return m.getName();
                }).collect(Collectors.toList());

        return result;
    }
}
