package com.example.backend.service.implementation;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.dto.response.StandardTreatmentDto;
import com.example.backend.model.dto.request.TreatmentRequestDto;
import com.example.backend.model.dto.response.TreatmentResponseDto;
import com.example.backend.model.dto.update.TreatmentUpdateDto;
import com.example.backend.model.entity.BloodPressureType;
import com.example.backend.model.entity.table.MedicalCondition;
import com.example.backend.model.entity.table.Medicine;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.entity.table.Treatment;
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

        List<MedicalConditionDto> medicalConditionDtos = getPatientsMedicalConditions(patient.getId());
        boolean hasIt = medicalConditionDtos.stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(medicalCondition.getName()));

        List<TreatmentResponseDto> patientTreatmentsForThisCondition = getPatientTreatments(patient.getEmail(), medicalCondition.getName());
        boolean alreadyTaking = patientTreatmentsForThisCondition.stream()
                .filter(t -> t.getEndingDate() == null)
                .anyMatch(t -> t.getMedicineName().equalsIgnoreCase(medicine.getName()));

        if(alreadyTaking) {
            throw new InvalidValues("Patient already takes that medicine");
        }

        if(!hasIt) {
            throw new InvalidValues("Patient does not have this medical condition");
        }

        if(!medicalCondition.getMedicines().contains(medicine)) {
            throw new InvalidValues("This medicine does not work for that medical condition");
        }

        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setMedicine(medicine);
        treatment.setMedicalCondition(medicalCondition);
        treatment.setDoses(treatmentRequestDto.getDoses());
        treatment.setComment(treatmentRequestDto.getComment());
        treatmentRepo.save(treatment);

        sendEmailService.sendTreatmentAdded(treatment.getId(), patient.getEmail());

        TreatmentResponseDto result = modelMapper.map(treatment, TreatmentResponseDto.class);
        return result;
    }

    @Override
    public TreatmentResponseDto getTreatmentById(Long id) {
        Treatment treatment = treatmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No treatment with this id"));
        TreatmentResponseDto result = modelMapper.map(treatment, TreatmentResponseDto.class);
        result.setPatientId(treatment.getPatient().getId());
        result.setMedicineName(treatment.getMedicine().getName());
        result.setMedicalConditionName(treatment.getMedicalCondition().getName());
        return result;
    }

    @Override
    public List<TreatmentResponseDto> getTreatmentsByTime(String patientEmail, String medicalConditionName, String fromDate, String toDate) {
        List<Treatment> treatments = treatmentRepo.findByDate(patientEmail, medicalConditionName, fromDate, toDate);
        List<TreatmentResponseDto> result = treatments
                .stream()
                .map(t -> {
                    TreatmentResponseDto tDto = modelMapper.map(t, TreatmentResponseDto.class);
                    tDto.setPatientId(t.getPatient().getId());
                    tDto.setMedicineName(t.getMedicine().getName());
                    tDto.setMedicalConditionName(t.getMedicalCondition().getName());
                    return tDto;
                }).collect(Collectors.toList());

        Comparator<TreatmentResponseDto> customComparator = Comparator.comparing(
                TreatmentResponseDto::getEndingDate,
                Comparator.nullsFirst(Comparator.reverseOrder())
        );

        result.sort(customComparator);
        return result;
    }

    @Override
    public TreatmentResponseDto updateTreatment(Long id, TreatmentUpdateDto treatmentUpdateDto) {
        Treatment treatment = treatmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No treatment with this id"));
        Medicine medicine = medicineRepo.findByName(treatmentUpdateDto.getMedicineName()).orElseThrow(() -> new ObjectNotFound("Medicine not found"));
        Patient patient = patientRepo.findById(treatment.getPatient().getId()).orElseThrow(() -> new ObjectNotFound("Patient not found"));
        treatment.setMedicine(medicine);
        treatment.setComment(treatmentUpdateDto.getComment());
        treatment.setDoses(treatmentUpdateDto.getDoses());
        treatmentRepo.save(treatment);

        sendEmailService.sendTreatmentChanged(treatment.getId(), patient.getEmail());

        return modelMapper.map(treatment, TreatmentResponseDto.class);
    }

    @Override
    public TreatmentResponseDto markAsEnded(Long id) {
        Treatment treatment = treatmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No treatment with this id"));
        Patient patient = patientRepo.findById(treatment.getPatient().getId()).orElseThrow(() -> new ObjectNotFound("Patient not found"));

        if(treatment.getEndingDate() != null) {
            throw new InvalidValues("Treatment already ended");
        }
        treatment.setEndingDate(new Date());
        treatmentRepo.save(treatment);

        sendEmailService.sendEndedTreatment(treatment.getId(), patient.getEmail());

        return modelMapper.map(treatment, TreatmentResponseDto.class);
    }


    @Override
    public List<TreatmentResponseDto> getPatientTreatments(String patientEmail, String medicalConditionName) {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("Patient not found"));
        MedicalCondition medicalCondition = medicalConditionRepo.findByName(medicalConditionName).orElseThrow(() -> new ObjectNotFound("Medical condition not found"));

        List <Treatment> treatments = patient.getTreatments();
        List <TreatmentResponseDto> result = treatments.stream()
                .filter(t -> t.getMedicalCondition().getName().equals(medicalConditionName))
                .map(t -> {
                    return modelMapper.map(t, TreatmentResponseDto.class);
                }).collect(Collectors.toList());

        Comparator<TreatmentResponseDto> customComparator = Comparator.comparing(
                TreatmentResponseDto::getEndingDate,
                Comparator.nullsFirst(Comparator.reverseOrder())
        );

        result.sort(customComparator);
        return result;
    }

    @Override
    public List<TreatmentResponseDto> getPatientOngoingTreatments(String patientEmail, String medicalConditionName) {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("Patient not found"));
        MedicalCondition medicalCondition = medicalConditionRepo.findByName(medicalConditionName).orElseThrow(() -> new ObjectNotFound("Medical condition not found"));

        List <Treatment> treatments = patient.getTreatments();
        List <TreatmentResponseDto> result = treatments.stream()
                .filter(t -> t.getMedicalCondition().getName().equals(medicalConditionName) && t.getEndingDate() == null)
                .map(t -> {
                    return modelMapper.map(t, TreatmentResponseDto.class);
                }).collect(Collectors.toList());

        Comparator<TreatmentResponseDto> customComparator = Comparator.comparing(
                TreatmentResponseDto::getEndingDate,
                Comparator.nullsFirst(Comparator.reverseOrder())
        );

        result.sort(customComparator);
        return result;
    }

    @Override
    public Page<TreatmentResponseDto> getPagedTreatments(String patientEmail, String medicalConditionName, Pageable pageable) {
        if(!patientRepo.findByEmail(patientEmail).isPresent())
            throw new ObjectNotFound("Patient not found");

        if(!medicalConditionRepo.findByName(medicalConditionName).isPresent())
            throw new ObjectNotFound("Medical condition not found");

        Page<Treatment> treatmentPage = treatmentRepo.findByPatientEmail(patientEmail, medicalConditionName, pageable);

        List <TreatmentResponseDto> result = treatmentPage.getContent()
                .stream()
                .map(t -> {
                    return modelMapper.map(t, TreatmentResponseDto.class);
                }).collect(Collectors.toList());

        return new PageImpl<>(result, pageable, treatmentPage.getTotalElements());
    }

    @Override
    public void setStandardTreatmentScheme(Long id, BloodPressureType bloodPressureType) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        if(bloodPressureType.toString().equals("Hypertension") || bloodPressureType.toString().equals("Hypotension")) {
            StandardTreatmentDto stdTreatment = standardTreatmentScheme(bloodPressureType);
            List<Treatment> treatmentsForMedicalCondition =
                    patient.getTreatments()
                            .stream()
                            .filter(t -> t.getMedicalCondition().getName().equals(stdTreatment.getMedicalConditionName()))
                            .collect(Collectors.toCollection(ArrayList::new));

            if (treatmentsForMedicalCondition.size() == 0) {
                Treatment treatment = new Treatment();
                treatment.setPatient(patient);
                treatment.setMedicalCondition(medicalConditionRepo.findByName(stdTreatment.getMedicalConditionName()).get());
                treatment.setMedicine(medicineRepo.findByName(stdTreatment.getMedicineName()).get());
                treatment.setDoses(stdTreatment.getDoses());
                treatment.setComment("Tratament standard");
                treatmentRepo.save(treatment);
                patient.getTreatments().add(treatment);
                patientRepo.save(patient);

                sendEmailService.sendTreatmentAdded(treatment.getId(), patient.getEmail());
            }
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

    public List<MedicalConditionDto> getPatientsMedicalConditions(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Patient not found"));

        List <MedicalConditionDto> result = patient.getPatient_medicalconditions().stream().map(m -> {
            MedicalConditionDto medicalConditionDto = new MedicalConditionDto();
            medicalConditionDto.setName(m.getMedicalCondition().getName());
            medicalConditionDto.setStartingDate(m.getStartingDate());
            medicalConditionDto.setEndingDate(m.getEndingDate());
            return medicalConditionDto;
        }).collect(Collectors.toList());

        return result;
    }
}
