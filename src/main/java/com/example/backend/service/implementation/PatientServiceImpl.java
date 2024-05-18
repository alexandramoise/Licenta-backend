package com.example.backend.service.implementation;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.update.PatientUpdateDto;
import com.example.backend.model.entity.*;
import com.example.backend.model.entity.table.*;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.AppointmentRepo;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.MedicalConditionRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final AppointmentRepo appointmentRepo;

    public PatientServiceImpl(PatientRepo patientRepo, DoctorRepo doctorRepo, MedicalConditionRepo medicalConditionRepo, ModelMapper modelMapper, BloodPressureService bloodPressureService, SendEmailService sendEmailService, TreatmentService treatmentService, PasswordEncoder passwordEncoder, EntityManager entityManager, AppointmentRepo appointmentRepo) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.medicalConditionRepo = medicalConditionRepo;
        this.modelMapper = modelMapper;
        this.bloodPressureService = bloodPressureService;
        this.sendEmailService = sendEmailService;
        this.treatmentService = treatmentService;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.appointmentRepo = appointmentRepo;
    }

    @Override
    public PatientResponseDto createAccount(String email, String doctorEmail) {
        // modified - using unique addresses, an address corresponds to only one account
        if (patientRepo.findByEmail(email).isPresent() || doctorRepo.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExists("An account with this email already exists");
        }

        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this id"));
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
    public PatientResponseDto updateAccount(PatientUpdateDto patientUpdateDto, String email) {
        Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No patient account for this address"));
        if(patientUpdateDto.getFirstName() != null)
            patient.setFirstName(patientUpdateDto.getFirstName());
        if(patientUpdateDto.getLastName() != null)
            patient.setLastName(patientUpdateDto.getLastName());
        if(patientUpdateDto.getDateOfBirth() != null)
            patient.setDateOfBirth(patientUpdateDto.getDateOfBirth());
        if(patientUpdateDto.getGender() != null)
            patient.setGender(patientUpdateDto.getGender());
        if (patientUpdateDto.getMedicalConditions() != null) {
            //log.info("Modific afectiunile");

            List<MedicalConditionDto> currentConditionsDto = getPatientsMedicalConditions(patient.getId());
            Set<String> currentConditionNames = currentConditionsDto.stream()
                    .map(MedicalConditionDto::getName)
                    .collect(Collectors.toSet());

            for (MedicalConditionDto dtoCondition : patientUpdateDto.getMedicalConditions()) {
                MedicalCondition dbCondition = medicalConditionRepo.findByName(dtoCondition.getName())
                        .orElseThrow(() -> new ObjectNotFound("No medical condition with this name: " + dtoCondition.getName()));

                log.info(dtoCondition.getName() + " incepand de la " + dtoCondition.getStartingDate() + " si sfarsita la: " + dtoCondition.getEndingDate());

                // it is a new condition for the patient
                if (!currentConditionNames.contains(dtoCondition.getName())) {
                    log.info("Afectiune noua: " + dtoCondition.getName());
                    PatientMedicalCondition pmc = new PatientMedicalCondition();
                    pmc.setMedicalCondition(dbCondition);
                    pmc.setPatient(patient);
                    pmc.setStartingDate(new Date());
                    pmc.setEndingDate(null);
                    patient.getPatient_medicalconditions().add(pmc);
                } else {
                    // condition exists and i need to check if the patient is ending it or restarting
                    Optional<PatientMedicalCondition> existingConditionOpt = patient.getPatient_medicalconditions()
                            .stream()
                            .filter(y -> y.getMedicalCondition().getName().equalsIgnoreCase(dtoCondition.getName()))
                            .findFirst();

                    if (existingConditionOpt.isPresent()) {
                        PatientMedicalCondition existingCondition = existingConditionOpt.get();
                        if (existingCondition.getEndingDate() == null && dtoCondition.getEndingDate() != null) {
                            log.info("Incheie: " + dtoCondition.getName());
                            existingCondition.setEndingDate(dtoCondition.getEndingDate());
                        } else if (existingCondition.getEndingDate() != null && dtoCondition.getEndingDate() == null) {
                            log.info("Reincepe: " + dtoCondition.getName());
                            existingCondition.setStartingDate(new Date());
                            existingCondition.setEndingDate(null);
                        }
                    }
                }
            }
        }

        patientRepo.save(patient);
        PatientResponseDto result = modelMapper.map(patient, PatientResponseDto.class);
        result.setFullName(patientUpdateDto.getFirstName().concat(" " + patientUpdateDto.getLastName()));
        return result;
    }

    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        String accountEmail = changePasswordDto.getEmail();
        if(!patientRepo.findByEmail(accountEmail).isPresent()) {
            throw new ObjectNotFound("There is no patient account with this email");
        }

        Patient patientAccount = patientRepo.findByEmail(accountEmail).get();

        // the temporary password sent through email
        String temporaryPassword = changePasswordDto.getOldPassword();

        boolean inputIsCorrect = passwordEncoder.matches(temporaryPassword, patientAccount.getPassword());
        if(! inputIsCorrect) {
            return false;
        }

        // the new password patient will get
        String frontendNewPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
        patientAccount.setPassword(frontendNewPassword);
        patientAccount.setFirstLoginEver(false);
        patientRepo.save(patientAccount);
        return true;
    }

    @Override
    public Boolean getFirstLoginEver(String email) {
        Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No patient account for this address"));
        return patient.getFirstLoginEver();
    }

    @Override
    public void requestPasswordChange(String email) {
        sendEmailService.sendResetPasswordEmail(email, "Patient");
    }

    @Override
    public List<PatientResponseDto> getAllPatients(String doctorEmail) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor account with this address"));
        List<Patient> patients = doctor.getPatients();
        List<PatientResponseDto> result =
                patients.stream().map((p) -> {
                    PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
                    pDto.setFullName(p.getFirstName().concat(" " + p.getLastName()));
                    pDto.setAge(calculateAge(p.getDateOfBirth()));
                    pDto.setTendency(p.getCurrentType());
                    pDto.setDoctorEmailAddress(doctorEmail);

                    return pDto;
                }).collect(Collectors.toList());
        return result;
    }

    @Override
    public Page<PatientResponseDto> getAllPagedPatients(String doctorEmail, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail)
                .orElseThrow(() -> new ObjectNotFound("No doctor account with this address"));

        Page<Patient> patientPage = patientRepo.findByDoctorEmail(doctorEmail, pageable);
        List<PatientResponseDto> result =
           patientPage
                .getContent()
                .stream()
                .map((p) -> {
                    PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
                    pDto.setFullName(p.getFirstName().concat(" " + p.getLastName()));
                    pDto.setAge(calculateAge(p.getDateOfBirth()));
                    pDto.setTendency(p.getCurrentType());
                    pDto.setDoctorEmailAddress(doctorEmail);

                    return pDto;
                }).collect(Collectors.toList());

        return new PageImpl<>(result, pageable, patientPage.getTotalElements());
    }

    @Override
    public Page<PatientResponseDto> getFilteredPagedPatients(String doctorEmail, String name, String gender, Integer maxAge, String type, Pageable pageable) {
        // Fetching patients using Criteria API with all filters except lastVisit
        Page<Patient> patientsPage = patientRepo.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email address"));
            predicates.add(criteriaBuilder.equal(root.get("doctor"), doctor));

            if (!name.isEmpty()) {
                Expression<String> fullNameVar1 = criteriaBuilder.concat(criteriaBuilder.concat(root.get("lastName"), " "), root.get("firstName"));
                Expression<String> fullNameVar2 = criteriaBuilder.concat(criteriaBuilder.concat(root.get("firstName"), " "), root.get("lastName"));
                Predicate namePredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(fullNameVar1), "%" + name.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(fullNameVar2), "%" + name.toLowerCase() + "%")
                );
                predicates.add(namePredicate);
            }

            if (!gender.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("gender")), gender.toLowerCase()));
            }

            if (maxAge > 0) {
                LocalDate maxBirthDate = LocalDate.now().minusYears(maxAge);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateOfBirth").as(LocalDate.class), maxBirthDate));
            }

            if (!type.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("currentType")), type.toLowerCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        // Map to DTOs
        Page<PatientResponseDto> patientDtos = patientsPage.map(p -> {
            PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
            pDto.setFullName(p.getFirstName() + " " + p.getLastName());
            pDto.setAge(calculateAge(p.getDateOfBirth()));
            pDto.setTendency(p.getCurrentType());
            pDto.setDoctorEmailAddress(doctorEmail);
            return pDto;
        });

        return patientDtos;
    }

    @Override
    public PatientResponseDto getPatientByEmail(String email) {
        Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        PatientResponseDto pDto = modelMapper.map(patient, PatientResponseDto.class);
        pDto.setFullName(patient.getFirstName().concat(" " + patient.getLastName()));
        pDto.setAge(calculateAge(patient.getDateOfBirth()));
        pDto.setTendency(patient.getCurrentType());
        pDto.setDoctorEmailAddress(patient.getDoctor().getEmail());

        return pDto;
    }

    @Override
    public PatientResponseDto getPatientById(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        PatientResponseDto pDto = modelMapper.map(patient, PatientResponseDto.class);
        pDto.setFullName(patient.getFirstName().concat(" " + patient.getLastName()));
        pDto.setAge(calculateAge(patient.getDateOfBirth()));
        pDto.setTendency(patient.getCurrentType());
        pDto.setDoctorEmailAddress(patient.getDoctor().getEmail());

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

    @Override
    public void setHypoOrHypertension(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        BloodPressureType type = patient.getCurrentType();

        List<PatientMedicalCondition> patientMedicalConditions = patient.getPatient_medicalconditions();

        if(type.equals("Hypertension")) {
            MedicalCondition hypertension = medicalConditionRepo.findByName("Hipertensiune").get();

            if(!patientMedicalConditions.contains(hypertension)) {
                PatientMedicalCondition pmc = new PatientMedicalCondition();
                pmc.setMedicalCondition(hypertension);
                pmc.setPatient(patient);
                pmc.setStartingDate(new Date());
                pmc.setEndingDate(null);
                patient.getPatient_medicalconditions().add(pmc);
                log.info("SETEZ TRATAMENT DIN PACIENT");
            }
            treatmentService.setStandardTreatmentScheme(patient.getId(), BloodPressureType.Hypertension);
        } else if(type.toString().equals("Hypotension")) {
            MedicalCondition hypotension = medicalConditionRepo.findByName("Hipotensiune").get();
            if(!patientMedicalConditions.contains(hypotension)) {
                log.info("SETEZ TRATAMENT DIN PACIENT");
                PatientMedicalCondition pmc = new PatientMedicalCondition();
                pmc.setMedicalCondition(hypotension);
                pmc.setPatient(patient);
                pmc.setStartingDate(new Date());
                pmc.setEndingDate(null);
                patient.getPatient_medicalconditions().add(pmc);
            }
            treatmentService.setStandardTreatmentScheme(patient.getId(), BloodPressureType.Hypotension);
        }

        patientRepo.save(patient);
    }

}
