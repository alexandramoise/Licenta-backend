package com.example.backend.service.implementation;

import com.example.backend.model.dto.*;
import com.example.backend.model.entity.*;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.MedicalConditionRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.BloodPressureService;
import com.example.backend.service.PatientService;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.TreatmentService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PatientServiceImpl implements PatientService  {
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final MedicalConditionRepo medicalConditionRepo;
    private final ModelMapper modelMapper;
    private final BloodPressureService bloodPressureService;
    private final SendEmailService sendEmailService;
    private final TreatmentService treatmentService;

    public PatientServiceImpl(PatientRepo patientRepo, DoctorRepo doctorRepo, MedicalConditionRepo medicalConditionRepo, ModelMapper modelMapper, BloodPressureService bloodPressureService, SendEmailService sendEmailService, TreatmentService treatmentService) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.medicalConditionRepo = medicalConditionRepo;
        this.modelMapper = modelMapper;
        this.bloodPressureService = bloodPressureService;
        this.sendEmailService = sendEmailService;
        this.treatmentService = treatmentService;
    }

    @Override
    public PatientResponseDto createAccount(String email, Long doctorId) {
        if (patientRepo.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExists("An account with this email already exists");
        }

        if(!doctorRepo.existsById(doctorId)) {
            throw new ObjectNotFound("No doctor with this id");
        }

        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new ObjectNotFound("No doctor with this id"));
        Patient patientAccount = sendEmailService.sendCreateAccountEmail(email, "Patient");
        patientAccount.setDoctor(doctor);
        log.info("In UserService: creare cont pacient - " + patientAccount.getEmail() + ", " + patientAccount.getPassword());
        PatientResponseDto result = modelMapper.map(patientAccount, PatientResponseDto.class);
        result.setDoctorEmailAddress(doctor.getEmail());
        return result;
    }

    /**
     * DE LUCRU LA MEDICAL CONDITIONS
     *
     * @param patientUpdateDto
     * @return
     */
    @Override
    public PatientResponseDto updateAccount(PatientUpdateDto patientUpdateDto) {
        String email = patientUpdateDto.getEmail();
        Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No doctor account for this address"));
        if(patientUpdateDto.getFirstName() != null)
            patient.setFirstName(patientUpdateDto.getFirstName());
        if(patientUpdateDto.getLastName() != null)
            patient.setLastName(patientUpdateDto.getLastName());
        if(patientUpdateDto.getDateOfBirth() != null)
            patient.setDateOfBirth(patientUpdateDto.getDateOfBirth());
        if(patientUpdateDto.getGender() != null)
            patient.setGender(patientUpdateDto.getGender());
        if(patientUpdateDto.getMedicalConditions() != null) {
            List<MedicalCondition> medicalConditions =
                    patientUpdateDto.getMedicalConditions().stream()
                            .map((m) -> {
                                return medicalConditionRepo.findByName(m.getName())
                                        .orElseThrow(() -> new ObjectNotFound("No medicinal condition with this name: " + m.getName()));
                            }).collect(Collectors.toCollection(ArrayList::new));

            patient.setMedicalConditions(medicalConditions);
        }

        patientRepo.save(patient);
        PatientResponseDto result = modelMapper.map(patient, PatientResponseDto.class);
        result.setFullName(patientUpdateDto.getFirstName().concat(" " + patientUpdateDto.getLastName()));
        return result;
    }

    /**
     * DE MODIFICAT SA RETURNEZE TOATE CAMPURILE - FUNCTIA SI RESPONSE DTO-UL
     * @param doctorEmail
     * @return
     */
    @Override
    public List<PatientResponseDto> getAllPatients(String doctorEmail) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor account with this address"));
        List<Patient> patients = doctor.getPatients();
        List<PatientResponseDto> result =
                patients.stream().map((p) -> {
                    PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
                    pDto.setFullName(p.getLastName().concat(" " + p.getFirstName()));

                    if(p.getBloodPressures().size() > 0) {
                        BloodPressureType type = bloodPressureService.getCurrentBPType(p.getEmail());
                        pDto.setTendency(type);
                    } else {
                        pDto.setTendency(BloodPressureType.valueOf("Normal"));
                    }

                    setHypoOrHypertension(p.getId());

                    if(p.getMedicalConditions().size() > 0) {
                        List <MedicalConditionRequestDto> medicalConditions = p.getMedicalConditions().stream()
                                .map(m -> {
                                    return modelMapper.map(m, MedicalConditionRequestDto.class);
                                }).collect(Collectors.toCollection(ArrayList::new));

                        pDto.setMedicalConditions(medicalConditions);
                    }

                    if(p.getTreatments().size() > 0) {
                        List<TreatmentResponseDto> treatments = p.getTreatments().stream()
                                .map(t -> {
                                    TreatmentResponseDto treatmentResponseDto = modelMapper.map(t, TreatmentResponseDto.class);
                                    treatmentResponseDto.setId(t.getId());
                                    return treatmentResponseDto;
                                }).collect(Collectors.toList());
                        treatments.sort(Comparator.comparing(TreatmentResponseDto::getStartingDate).reversed());
                        pDto.setTreatments(treatments);
                    }
                    pDto.setDoctorEmailAddress(doctorEmail);
                    pDto.setAge(calculateAge(p.getDateOfBirth()));
                    pDto.setBloodPressures(bloodPressureService.getPatientBloodPressures(p.getEmail()));
                    return pDto;
                }).toList();
        return result;
     }

    @Override
    public Page<PatientResponseDto> getAllPagedPatients(String doctorEmail, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail)
                .orElseThrow(() -> new ObjectNotFound("No doctor account with this address"));

        List<PatientResponseDto> result =
            patientRepo.findByDoctorEmail(doctorEmail, pageable)
                .getContent()
                .stream()
                .map((p) -> {
                    PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
                    pDto.setFullName(p.getLastName().concat(" " + p.getFirstName()));

                    if(p.getBloodPressures().size() > 0) {
                        BloodPressureType type = bloodPressureService.getCurrentBPType(p.getEmail());
                        pDto.setTendency(type);
                    } else {
                        pDto.setTendency(BloodPressureType.valueOf("Normal"));
                    }

                    setHypoOrHypertension(p.getId());

                    if(p.getMedicalConditions().size() > 0) {
                        List <MedicalConditionRequestDto> medicalConditions = p.getMedicalConditions().stream()
                                .map(m -> {
                                    return modelMapper.map(m, MedicalConditionRequestDto.class);
                                }).collect(Collectors.toCollection(ArrayList::new));

                        pDto.setMedicalConditions(medicalConditions);
                    }

                    if(p.getTreatments().size() > 0) {
                        List<TreatmentResponseDto> treatments = p.getTreatments().stream()
                                .map(t -> {
                                    TreatmentResponseDto treatmentResponseDto = modelMapper.map(t, TreatmentResponseDto.class);
                                    treatmentResponseDto.setId(t.getId());
                                    return treatmentResponseDto;
                                }).collect(Collectors.toList());
                        treatments.sort(Comparator.comparing(TreatmentResponseDto::getStartingDate).reversed());
                        pDto.setTreatments(treatments);
                    }
                    pDto.setDoctorEmailAddress(doctorEmail);
                    pDto.setAge(calculateAge(p.getDateOfBirth()));
                    pDto.setBloodPressures(bloodPressureService.getPatientBloodPressures(p.getEmail()));
                    return pDto;
                }).toList();

        return new PageImpl<>(result, pageable, result.size());
    }

    @Override
    public PatientResponseDto getPatientById(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        PatientResponseDto pDto = modelMapper.map(patient, PatientResponseDto.class);
        pDto.setFullName(patient.getLastName().concat(" " + patient.getFirstName()));
        pDto.setDoctorEmailAddress(patient.getDoctor().getEmail());
        pDto.setAge(calculateAge(patient.getDateOfBirth()));
        if(patient.getBloodPressures().size() > 0) {
            BloodPressureType type = bloodPressureService.getCurrentBPType(patient.getEmail());
            pDto.setTendency(type);
        } else {
            pDto.setTendency(BloodPressureType.valueOf("Normal"));
        }
        pDto.setBloodPressures(bloodPressureService.getPatientBloodPressures(patient.getEmail()));

        /* setting the medical condition based on patient's tendency: hypo - / hypertension */
        setHypoOrHypertension(patient.getId());

        List <MedicalConditionRequestDto> medicalConditions = patient.getMedicalConditions()
                .stream()
                .map(m -> {
                    return modelMapper.map(m, MedicalConditionRequestDto.class);
                }).collect(Collectors.toCollection(ArrayList::new));
        pDto.setMedicalConditions(medicalConditions);

        List <TreatmentResponseDto> treatments = patient.getTreatments().stream()
                .map(t -> {
                    TreatmentResponseDto treatmentResponseDto = modelMapper.map(t, TreatmentResponseDto.class);
                    treatmentResponseDto.setId(t.getId());
                    return treatmentResponseDto;
                }).collect(Collectors.toList());
        treatments.sort(Comparator.comparing(TreatmentResponseDto::getStartingDate).reversed());
        pDto.setTreatments(treatments);

        return pDto;
    }

    @Override
    public Integer calculateAge(Date dateOfBirth) {
        LocalDate bdayLocalDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentLocalDate = LocalDate.now();
        Period period = Period.between(bdayLocalDate, currentLocalDate);
        return period.getYears();
    }

    @Override
    public List<String> getPatientsMedicalConditions(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Patient not found"));

        List <String> result = patient.getMedicalConditions().stream().map(m -> {
            return m.getName();
        }).toList();
        return result;
    }

    @Override
    public void setHypoOrHypertension(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        PatientResponseDto pDto = modelMapper.map(patient, PatientResponseDto.class);
        if(patient.getBloodPressures().size() > 0) {
            BloodPressureType type = bloodPressureService.getCurrentBPType(patient.getEmail());
            pDto.setTendency(type);

            if(pDto.getTendency().toString().equals("Hypertension")) {
                MedicalCondition hypertension = medicalConditionRepo.findByName("Hipertensiune").get();
                if(!patient.getMedicalConditions().contains(hypertension))
                    patient.getMedicalConditions().add(hypertension);
                treatmentService.setStandardTreatmentScheme(patient.getId(), BloodPressureType.Hypertension);
            } else if(pDto.getTendency().toString().equals("Hypotension")) {
                MedicalCondition hypotension = medicalConditionRepo.findByName("Hipotensiune").get();
                if(!patient.getMedicalConditions().contains(hypotension))
                    patient.getMedicalConditions().add(hypotension);
                treatmentService.setStandardTreatmentScheme(patient.getId(), BloodPressureType.Hypotension);
            }
        }
        patientRepo.save(patient);
    }

}
