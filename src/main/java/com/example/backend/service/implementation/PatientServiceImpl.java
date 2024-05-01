package com.example.backend.service.implementation;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.update.PatientUpdateDto;
import com.example.backend.model.entity.*;
import com.example.backend.model.entity.table.*;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.MedicalConditionRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.*;
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

    public PatientServiceImpl(PatientRepo patientRepo, DoctorRepo doctorRepo, MedicalConditionRepo medicalConditionRepo, ModelMapper modelMapper, BloodPressureService bloodPressureService, SendEmailService sendEmailService, TreatmentService treatmentService, PasswordEncoder passwordEncoder) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.medicalConditionRepo = medicalConditionRepo;
        this.modelMapper = modelMapper;
        this.bloodPressureService = bloodPressureService;
        this.sendEmailService = sendEmailService;
        this.treatmentService = treatmentService;
        this.passwordEncoder = passwordEncoder;
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
        if(patientUpdateDto.getMedicalConditions() != null) {
            List<PatientMedicalCondition> medicalConditions =
                    patientUpdateDto.getMedicalConditions().stream()
                            .map((m) -> {
                                MedicalCondition mc = medicalConditionRepo.findByName(m.getName())
                                        .orElseThrow(() -> new ObjectNotFound("No medicinal condition with this name: " + m.getName()));
                                List<MedicalConditionDto> patientMedicalConditions = getPatientsMedicalConditions(patient.getId());

                                List<String> medicalConditionNames = patientMedicalConditions.stream()
                                        .map(MedicalConditionDto::getName)
                                        .collect(Collectors.toList());

                                log.info(m.getName() + " e boala " + " la data de " + m.getStartingDate() + " incheiata la " + m.getEndingDate());
                                if(!medicalConditionNames.contains(m.getName()) && m.getEndingDate() == null) {
                                    PatientMedicalCondition pmc = new PatientMedicalCondition();
                                    pmc.setMedicalCondition(mc);
                                    pmc.setPatient(patient);
                                    pmc.setStartingDate(m.getStartingDate());
                                    pmc.setEndingDate(m.getEndingDate());
                                    return pmc;
                                } else if(medicalConditionNames.contains(m.getName()) && m.getEndingDate() != null){
                                    PatientMedicalCondition pmc = patient.getPatient_medicalconditions()
                                            .stream()
                                            .filter(y -> y.getMedicalCondition().getName().equalsIgnoreCase(m.getName()))
                                            .findFirst().get();
                                    pmc.setEndingDate(m.getEndingDate());
                                    return pmc;
                                } else {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull) // sterg valorile null din stream
                            .collect(Collectors.toCollection(ArrayList::new));

            patient.setPatient_medicalconditions(medicalConditions);
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
            throw new ObjectNotFound("There is no doctor account with this email");
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
    public List<PatientResponseDto> getFilteredPatients(String doctorEmail, String name, String gender, Integer maxAge, String type, Integer lastVisit) {
        List<Patient> patients = patientRepo.findAllByDoctorEmail(doctorEmail);
        return patients.stream()
                .filter(p -> name.equals("null") || (p.getLastName() != null && p.getLastName().toLowerCase().contains(name.toLowerCase())) || (p.getFirstName() != null && p.getFirstName().toLowerCase().contains(name.toLowerCase())))
                .filter(p -> gender.equals("null") || gender.equalsIgnoreCase(p.getGender().toString()))
                .filter(p ->  maxAge <= 0 || Period.between(p.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears() <= maxAge)
                .filter(p -> type.equals("null") || type.equalsIgnoreCase(p.getCurrentType().toString()))
                .filter(p -> {
                    if (lastVisit == null || lastVisit <= 0) {
                        return true;
                    }
                    return p.getAppointments().stream()
                            .filter(a -> a.getTime().before(new Date()))
                            .max(Comparator.comparing(Appointment::getTime))
                            .map(lastAppointment -> Period.between(lastAppointment.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getDays() <= lastVisit)
                            .orElse(false);
                })
                .map(p -> {
                    PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
                    pDto.setFullName(p.getFirstName().concat(" " + p.getLastName()));
                    pDto.setAge(calculateAge(p.getDateOfBirth()));
                    pDto.setTendency(p.getCurrentType());
                    pDto.setDoctorEmailAddress(doctorEmail);
                    return pDto;
                })
                .collect(Collectors.toList());
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
    public Page<PatientResponseDto> getFilteredPagedPatients(String doctorEmail, String name, String gender, Integer maxAge, String type, Integer lastVisit, Pageable pageable) {
        Page<Patient> patientPage = patientRepo.findByDoctorEmail(doctorEmail, pageable);

        List<PatientResponseDto> result = getFilteredPatients(doctorEmail, name, gender, maxAge, type, lastVisit);

        return new PageImpl<>(result, pageable, patientPage.getTotalElements());
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
