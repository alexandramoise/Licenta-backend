package com.example.backend.service.implementation;

import com.example.backend.model.dto.*;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.exception.AccountAlreadyExists;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.service.DoctorService;
import com.example.backend.service.SendEmailService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.query.Page;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;

@Service
@Log4j2
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepo doctorRepo;
    private final SendEmailService sendEmailService;
    private final ModelMapper modelMapper;

    public DoctorServiceImpl(DoctorRepo doctorRepo, SendEmailService sendEmailService, ModelMapper modelMapper) {
        this.doctorRepo = doctorRepo;
        this.sendEmailService = sendEmailService;
        this.modelMapper = modelMapper;
    }

    @Override
    public DoctorResponseDto createAccount(String email) {
        if (doctorRepo.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExists("An account with this email already exists");
        }

        Doctor doctorAccount = sendEmailService.sendCreateAccountEmail(email, "Doctor");
        log.info("In UserService: creare cont doctor - " + doctorAccount.getEmail() + ", " + doctorAccount.getPassword());
        return modelMapper.map(doctorAccount, DoctorResponseDto.class);
    }

    @Override
    public DoctorResponseDto updateAccount(DoctorUpdateDto doctorUpdateDto) {
        String email = doctorUpdateDto.getEmail();
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

    /***
     * TO WORK ON PASSWORD'S STUFF
     * @param changePasswordDto
     * @return
     */
    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        String accountEmail = changePasswordDto.getEmail();
        if(!doctorRepo.findByEmail(accountEmail).isPresent()) {
            throw new ObjectNotFound("There is no doctor account with this email");
        }
        Doctor doctorAccount = doctorRepo.findByEmail(accountEmail).get();
        String frontendNewPassword = changePasswordDto.getNewPassword().concat("HASHED");
        System.out.println("NEW PASSWORD: " + frontendNewPassword);
        System.out.println("DOCTOR'S OLD PASSWORD: " + doctorAccount.getPassword());
        if(!changePasswordDto.getOldPassword().equals(doctorAccount.getPassword().substring(0,15))) {
            return false;
        }
        doctorAccount.setPassword(frontendNewPassword);
        doctorAccount.setFirstLoginEver(false);
        doctorRepo.save(doctorAccount);
        return true;
    }

    @Override
    public DoctorResponseDto updateDoctor(DoctorUpdateDto doctorUpdateDto) {
        String email = doctorUpdateDto.getEmail();
        if(!doctorRepo.findByEmail(email).isPresent()) {
            throw new ObjectNotFound("There is no doctor account with this email");
        }
        Doctor doctorAccount = doctorRepo.findByEmail(email).get();
        String newFirstName = doctorUpdateDto.getFirstName();
        String newLastName = doctorUpdateDto.getLastName();
        doctorAccount.setFirstName(newFirstName);
        doctorAccount.setLastName(newLastName);
        doctorRepo.save(doctorAccount);
        return modelMapper.map(doctorAccount, DoctorResponseDto.class);
    }

    @Override
    public DoctorResponseDto getDoctorByEmail(String email) {
        return null;
    }

    @Override
    public Page getDoctorsPatients(Pageable pageable) {
        return null;
    }
}
