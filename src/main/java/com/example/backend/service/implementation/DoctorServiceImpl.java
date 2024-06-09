package com.example.backend.service.implementation;

import com.example.backend.model.dto.response.DoctorResponseDto;
import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.update.DoctorUpdateDto;
import com.example.backend.model.entity.table.Doctor;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.DoctorService;
import com.example.backend.service.SendEmailService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.print.Doc;

@Service
@Log4j2
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final SendEmailService sendEmailService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public DoctorServiceImpl(DoctorRepo doctorRepo, PatientRepo patientRepo, SendEmailService sendEmailService, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.sendEmailService = sendEmailService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public DoctorResponseDto createAccount(String email) {
        // modified - using unique addresses, an address corresponds to only one account
        if (doctorRepo.findByEmail(email).isPresent() || patientRepo.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExists("An account with this email already exists");
        }

        Doctor doctorAccount = sendEmailService.sendCreateAccountEmail(email, "Doctor");
        log.info("In UserService: creare cont doctor - " + doctorAccount.getEmail() + ", " + doctorAccount.getPassword());
        return modelMapper.map(doctorAccount, DoctorResponseDto.class);
    }

    @Override
    public Boolean getFirstLoginEver(String email) {
        Doctor doctor = doctorRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No doctor account for this address"));
        return doctor.getFirstLoginEver();
    }

    @Override
    public DoctorResponseDto updateAccount(DoctorUpdateDto doctorUpdateDto, String email) {
        Doctor doctor = doctorRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No doctor account for this address"));
        if(doctorUpdateDto.getFirstName() != null)
            doctor.setFirstName(doctorUpdateDto.getFirstName());
        if(doctorUpdateDto.getLastName() != null)
            doctor.setLastName(doctorUpdateDto.getLastName());
        doctorRepo.save(doctor);
        DoctorResponseDto result = modelMapper.map(doctor, DoctorResponseDto.class);
        result.setFullName(doctorUpdateDto.getFirstName().concat(" " + doctorUpdateDto.getLastName()));
        return result;
    }

    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        String accountEmail = changePasswordDto.getEmail();
        if(!doctorRepo.findByEmail(accountEmail).isPresent()) {
            throw new ObjectNotFound("There is no doctor account with this email");
        }

        Doctor doctorAccount = doctorRepo.findByEmail(accountEmail).get();

        // the temporary password sent through email
        String temporaryPassword = changePasswordDto.getOldPassword();

        boolean inputIsCorrect = passwordEncoder.matches(temporaryPassword, doctorAccount.getPassword());
        if(! inputIsCorrect) {
            return false;
        }

        // the new password patient will get
        String frontendNewPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
        doctorAccount.setPassword(frontendNewPassword);
        doctorAccount.setFirstLoginEver(true);
        doctorRepo.save(doctorAccount);
        return true;
    }

    @Override
    public void requestPasswordChange(String email) {
        sendEmailService.sendResetPasswordEmail(email, "Doctor");
    }

    @Override
    public void acceptTerms(String email) {
        Doctor doctor = doctorRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("Doctor not found"));
        doctor.setAcceptedTermsAndConditions(true);
        doctorRepo.save(doctor);
    }

    @Override
    public DoctorResponseDto getDoctorByEmail(String email) {
        Doctor doctor = doctorRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        DoctorResponseDto result = modelMapper.map(doctor, DoctorResponseDto.class);
        result.setFullName(doctor.getFirstName().concat(" " + doctor.getLastName()));
        return result;
    }
}
