package com.example.backend.service.implementation;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.MedicalConditionService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalConditionServiceImpl implements MedicalConditionService {
    private final PatientRepo patientRepo;

    public MedicalConditionServiceImpl(PatientRepo patientRepo) {
        this.patientRepo = patientRepo;
    }

    @Override
    public List<MedicalConditionDto> getPatientCurrentMedicalConditions(String patientEmail) {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("Patient not found"));

        List <MedicalConditionDto> result = patient.getPatient_medicalconditions().stream()
                .filter(m -> m.getEndingDate() == null)
                .map(m -> {
                    MedicalConditionDto medicalConditionDto = new MedicalConditionDto();
                    medicalConditionDto.setName(m.getMedicalCondition().getName());
                    medicalConditionDto.setStartingDate(m.getStartingDate());
                    medicalConditionDto.setEndingDate(m.getEndingDate());
                    return medicalConditionDto;
                }).collect(Collectors.toList());

        Comparator<MedicalConditionDto> customComparator = Comparator.comparing(
                MedicalConditionDto::getEndingDate,
                Comparator.nullsFirst(Comparator.reverseOrder())
        );

        result.sort(customComparator);
        return result;
    }

    @Override
    public List<MedicalConditionDto> getPatientAllMedicalConditions(String patientEmail) {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("Patient not found"));

        List <MedicalConditionDto> result = patient.getPatient_medicalconditions().stream()
                .map(m -> {
                    MedicalConditionDto medicalConditionDto = new MedicalConditionDto();
                    medicalConditionDto.setName(m.getMedicalCondition().getName());
                    medicalConditionDto.setStartingDate(m.getStartingDate());
                    medicalConditionDto.setEndingDate(m.getEndingDate());
                    return medicalConditionDto;
                }).collect(Collectors.toList());

        Comparator<MedicalConditionDto> customComparator = Comparator.comparing(
                MedicalConditionDto::getEndingDate,
                Comparator.nullsFirst(Comparator.reverseOrder())
        );

        result.sort(customComparator);
        return result;
    }
}
