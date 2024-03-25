package com.example.backend.service.implementation;

import com.example.backend.model.dto.*;
import com.example.backend.model.entity.*;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.exception.InvalidAccountType;
import com.example.backend.model.exception.InvalidCredentials;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.model.repo.UserRepo;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class UserServiceImpl implements UserService  {
    private final Map<String, UserRepo> userRepositories;
    private final SendEmailService sendEmailService;
    private final ModelMapper modelMapper;

    public UserServiceImpl(DoctorRepo doctorRepo, PatientRepo patientRepo, SendEmailService sendEmailService, ModelMapper modelMapper) {
        this.sendEmailService = sendEmailService;
        this.modelMapper = modelMapper;
        this.userRepositories = new HashMap<>();
        this.userRepositories.put("Doctor", doctorRepo);
        this.userRepositories.put("Patient", patientRepo);
    }

    @Override
    public Object createAccount(String email, String accountType, Long doctorId) {
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        if (repo.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExists("An account with this email already exists");
        }

        if(accountType.equals("Doctor")) {
            Doctor doctorAccount = sendEmailService.sendCreateAccountEmail(email, "Doctor");
            log.info("In UserService: creare cont doctor - " + doctorAccount.getEmail() + ", " + doctorAccount.getPassword());
            return modelMapper.map(doctorAccount, DoctorResponseDto.class);
        } else {
            DoctorRepo doctorRepo = (DoctorRepo) this.userRepositories.get("Doctor");
            Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new ObjectNotFound("No doctor with this id"));
            Patient patientAccount = sendEmailService.sendCreateAccountEmail(email, "Patient");
            patientAccount.setDoctor(doctor);
            log.info("In UserService: creare cont pacient - " + patientAccount.getEmail() + ", " + patientAccount.getPassword());
            PatientResponseDto result = modelMapper.map(patientAccount, PatientResponseDto.class);
            result.setDoctorEmailAddress(doctor.getEmail());
            return result;
        }
    }

    /**
     * WILL WORK ON HASHED PASSWORD
     * @param changePasswordDto
     * @param accountType
     * @return
     * @throws IllegalAccessException
     */
    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto, String accountType){
        String email = changePasswordDto.getEmail();
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }
        if (!repo.findByEmail(email).isPresent()) {
            throw new ObjectNotFound("There is no account with this email");
        }

        User account = (User) repo.findByEmail(email).get();
        String frontendNewPassword = changePasswordDto.getNewPassword();
        if(!changePasswordDto.getOldPassword().equals(account.getPassword().substring(0,15))) {
            throw new InvalidCredentials("Incorrect password");
        }
        account.setPassword(frontendNewPassword);
        account.setFirstLoginEver(false);

        repo.save(account);
        return true;
    }

    /**
     * REFACTOR MASIV LA ASTEA DOUA
     *
     * @param updateDto
     * @param accountType
     * @return
     */
    @Override
    public Object getAccount(Object updateDto, String accountType) {
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        if(accountType.equals("Doctor")) {
            DoctorUpdateDto doctorUpdateDto = (DoctorUpdateDto) updateDto;
            String email = doctorUpdateDto.getEmail();
            DoctorRepo doctorRepo = (DoctorRepo) repo;
            Doctor doctor = doctorRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No doctor account for this address"));
            return doctor;
        } else {
            PatientUpdateDto patientUpdateDto = (PatientUpdateDto) updateDto;
            String email = patientUpdateDto.getEmail();
            PatientRepo patientRepo = (PatientRepo) repo;
            Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No doctor account for this address"));
            return patient;
        }
    }

    @Override
    public Object updateAccount(Object updateDto, String accountType)  throws InvalidAccountType {
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        Object objectFromUpdateDtoEmail = getAccount(updateDto, accountType);
        if(accountType.equals("Doctor")) {
            Doctor doctor = (Doctor) objectFromUpdateDtoEmail;
            DoctorUpdateDto doctorUpdateDto = (DoctorUpdateDto) updateDto;
            if(doctorUpdateDto.getFirstName() != null)
                doctor.setFirstName(doctorUpdateDto.getFirstName());
            if(doctorUpdateDto.getLastName() != null)
                doctor.setLastName(doctorUpdateDto.getLastName());
            repo.save(doctor);
            DoctorResponseDto result = modelMapper.map(doctor, DoctorResponseDto.class);
            result.setFullName(doctorUpdateDto.getFirstName().concat(" " + doctorUpdateDto.getLastName()));
            return result;
        } else {
            Patient patient = (Patient) objectFromUpdateDtoEmail;
            PatientUpdateDto patientUpdateDto = (PatientUpdateDto) updateDto;
            if(patientUpdateDto.getFirstName() != null)
                patient.setFirstName(patientUpdateDto.getFirstName());
            if(patientUpdateDto.getLastName() != null)
                patient.setLastName(patientUpdateDto.getLastName());
            if(patientUpdateDto.getDateOfBirth() != null)
                patient.setDateOfBirth(patientUpdateDto.getDateOfBirth());
//            if(patientUpdateDto.getGender() != null)
//                patient.setGender(patientUpdateDto.getGender());
//            if(patientUpdateDto.getMedicalConditions().size() > 0)
//                patient.setMedicalConditions(patientUpdateDto.getMedicalConditions());
            repo.save(patient);
            PatientResponseDto result = modelMapper.map(patient, PatientResponseDto.class);
            result.setFullName(patientUpdateDto.getFirstName().concat(" " + patientUpdateDto.getLastName()));
            return result;
        }
    }
}
