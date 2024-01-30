package com.example.backend.service.implementation;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.dto.DoctorResponseDto;
import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Patient;
import com.example.backend.model.entity.User;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.AccountNotFound;
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
    public Object createAccount(String email, String accountType) {
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
            Patient patientAccount = sendEmailService.sendCreateAccountEmail(email, "Patient");
            log.info("In UserService: creare cont doctor - " + patientAccount.getEmail() + ", " + patientAccount.getPassword());
            return modelMapper.map(patientAccount, PatientResponseDto.class);
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
            throw new AccountNotFound("There is no account with this email");
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
    @Override
    public Object updateAccount(Object updateDto, String accountType)  throws IllegalAccessException {
        return new Object();
    }
}
