package com.example.backend.service.implementation;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.dto.DoctorResponseDto;
import com.example.backend.model.dto.DoctorUpdateDto;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.service.DoctorService;
import com.example.backend.service.SendEmailService;
import org.hibernate.query.Page;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;

@Service
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepo doctorRepo;
    private final SendEmailService sendEmailService;
    private final ModelMapper modelMapper;

    public DoctorServiceImpl(DoctorRepo doctorRepo, SendEmailService sendEmailService, ModelMapper modelMapper) {
        this.doctorRepo = doctorRepo;
        this.sendEmailService = sendEmailService;
        this.modelMapper = modelMapper;
    }

//    @Override
//    public DoctorResponseDto createDoctorAccount(String email) {
//        if(doctorRepo.findByEmail(email).isPresent()) {
//            throw new AccountAlreadyExists("A doctor account with this email already exists");
//        }
//        sendEmailService.sendCreateAccountEmail(email, "Doctor");
//        return new DoctorResponseDto();
//    }

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
