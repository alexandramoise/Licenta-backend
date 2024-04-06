package com.example.backend.service.implementation;

import com.example.backend.model.dto.StandardTreatmentDto;
import com.example.backend.model.dto.TreatmentRequestDto;
import com.example.backend.model.dto.TreatmentResponseDto;
import com.example.backend.model.entity.*;
import com.example.backend.model.exception.EmptyList;
import com.example.backend.model.exception.InvalidValues;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.*;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.TreatmentService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class TreatmentServiceImpl implements TreatmentService {
    private final TreatmentRepo treatmentRepo;
    private final MedicineRepo medicineRepo;
    private final MedicalConditionRepo medicalConditionRepo;
    private final SendEmailService sendEmailService;
    private final ModelMapper modelMapper;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public TreatmentServiceImpl(TreatmentRepo treatmentRepo, MedicineRepo medicineRepo, MedicalConditionRepo medicalConditionRepo, SendEmailService sendEmailService, ModelMapper modelMapper, DoctorRepo doctorRepo, PatientRepo patientRepo) {
        this.treatmentRepo = treatmentRepo;
        this.medicineRepo = medicineRepo;
        this.medicalConditionRepo = medicalConditionRepo;
        this.sendEmailService = sendEmailService;
        this.modelMapper = modelMapper;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    @Override
    public TreatmentResponseDto addTreatment(TreatmentRequestDto treatmentRequestDto) {
        Patient patient = patientRepo.findById(treatmentRequestDto.getPatientId()).orElseThrow(() -> new ObjectNotFound("Patient not found"));
        MedicalCondition medicalCondition = medicalConditionRepo.findByName(treatmentRequestDto.getMedicalConditionName()).orElseThrow(() -> new ObjectNotFound("Medical condition not found"));
        Medicine medicine = medicineRepo.findByName(treatmentRequestDto.getMedicineName()).orElseThrow(() -> new ObjectNotFound("Medicine not found"));

        if(!patient.getMedicalConditions().contains(medicalCondition)) {
            throw new InvalidValues("Patient does not have this medical condition");
        }
        if(!medicalCondition.getMedicines().contains(medicine)) {
            throw new InvalidValues("This medicine does not work for that medical condition");
        }

        if(patient.getTreatments().size() != 0) {
            List<Treatment> treatments = patient.getTreatments()
                    .stream()
                    .filter(t -> t.getMedicalCondition().getName().equals(medicalCondition.getName()))
                    .collect(Collectors.toList());
            if (!treatments.isEmpty()) {
                treatments.sort(Comparator.comparing(Treatment::getStartingDate).reversed());
                Treatment latestTreatment = treatments.get(0);
                latestTreatment.setEndingDate(new Date());
            }
        }


        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setMedicine(medicine);
        treatment.setMedicalCondition(medicalCondition);
        treatment.setDoses(treatmentRequestDto.getDoses());
        treatmentRepo.save(treatment);

        TreatmentResponseDto result = modelMapper.map(treatment, TreatmentResponseDto.class);
        result.setId(treatment.getId());
        return result;
    }



    @Override
    public List<TreatmentResponseDto> getPatientTreatments(String patientEmail, String medicalConditionName) {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("Patient not found"));
        MedicalCondition medicalCondition = medicalConditionRepo.findByName(medicalConditionName).orElseThrow(() -> new ObjectNotFound("Medical condition not found"));

        List <Treatment> treatments = patient.getTreatments();
        List <TreatmentResponseDto> result = treatments.stream()
                .filter(t -> t.getMedicalCondition().getName().equals(medicalConditionName))
                .map(t -> {
                    TreatmentResponseDto treatmentResponseDto = modelMapper.map(t, TreatmentResponseDto.class);
                    treatmentResponseDto.setId(t.getId());
                    return treatmentResponseDto;
                }).collect(Collectors.toList());
        result.sort(Comparator.comparing(TreatmentResponseDto::getStartingDate).reversed());
        return result;
    }

    @Override
    public Page<TreatmentResponseDto> getPagedTreatments(String patientEmail, String medicalConditionName, Pageable pageable) {
        if(!patientRepo.findByEmail(patientEmail).isPresent())
            throw new ObjectNotFound("Patient not found");

        if(!medicalConditionRepo.findByName(medicalConditionName).isPresent())
            throw new ObjectNotFound("Medical condition not found");

        List <TreatmentResponseDto> result = treatmentRepo.findByPatientEmail(patientEmail, medicalConditionName, pageable).getContent()
                .stream()
                .map(t -> {
                    TreatmentResponseDto treatmentResponseDto = modelMapper.map(t, TreatmentResponseDto.class);
                    treatmentResponseDto.setId(t.getId());
                    return treatmentResponseDto;
                }).collect(Collectors.toList());

        return new PageImpl<>(result, pageable, result.size());
    }

    @Override
    public void setStandardTreatmentScheme(Long id, BloodPressureType bloodPressureType) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        StandardTreatmentDto stdTreatment = standardTreatmentScheme(bloodPressureType);
        List<Treatment> treatmentsForMedicalCondition =
                patient.getTreatments()
                .stream()
                .filter(t -> t.getMedicalCondition().getName().equals(stdTreatment.getMedicalConditionName()))
                        .collect(Collectors.toCollection(ArrayList::new));

        if(treatmentsForMedicalCondition.size() == 0) {
            Treatment treatment = new Treatment();
            treatment.setPatient(patient);
            treatment.setMedicalCondition(medicalConditionRepo.findByName(stdTreatment.getMedicalConditionName()).get());
            treatment.setMedicine(medicineRepo.findByName(stdTreatment.getMedicineName()).get());
            treatment.setDoses(stdTreatment.getDoses());
            treatmentRepo.save(treatment);
            patient.getTreatments().add(treatment);
            patientRepo.save(patient);
        }
    }

    @Override
    public StandardTreatmentDto standardTreatmentScheme(BloodPressureType bloodPressureType) {
        StandardTreatmentDto result = new StandardTreatmentDto();
        if(bloodPressureType.toString().equals("Hypertension")) {
            result.setMedicineName("Enalapril");
            result.setMedicalConditionName("Hipertensiune");
            result.setDoses(1);
        } else if(bloodPressureType.toString().equals("Hypotension")) {
            result.setMedicineName("Astonin");
            result.setMedicalConditionName("Hipotensiune");
            result.setDoses(1);
        }

        return result;
    }
}
