package com.example.backend.controller;

import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.response.DoctorResponseDto;
import com.example.backend.model.dto.update.DoctorUpdateDto;
import com.example.backend.model.exception.InvalidCredentials;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.service.DoctorService;
import com.example.backend.service.SendEmailService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@Log4j2
public class DoctorController {
    private final DoctorService doctorService;
    private final SendEmailService sendEmailService;

    public DoctorController(DoctorService doctorService, SendEmailService sendEmailService) {
        this.doctorService = doctorService;
        this.sendEmailService = sendEmailService;
    }

    @Transactional
    @PostMapping("/new")
    public ResponseEntity<?> doctorInitiatesAccount(@RequestParam(name = "email", required = true) String email) {
        DoctorResponseDto doctor = doctorService.createAccount(email);
        log.info("In DoctorController: trimit - " + doctor.getEmail());
        if (doctor != null) {
            return new ResponseEntity<>("Registered succesfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<DoctorResponseDto> getDoctorByEmail(@RequestParam(name = "email", required = true) String email) {
        DoctorResponseDto doctor = doctorService.getDoctorByEmail(email);
        if (doctor != null) {
            return new ResponseEntity<DoctorResponseDto>(doctor, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestNewPassword(@RequestParam(required = true) String email) throws ObjectNotFound {
        doctorService.requestPasswordChange(email);
        return new ResponseEntity<>("Request received, check your email", HttpStatus.OK);
    }

    @Transactional
    @PutMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordDto changePasswordDto) throws ObjectNotFound, InvalidCredentials {
        boolean passwordChanged = doctorService.changePassword(changePasswordDto);
        if (passwordChanged) {
            return new ResponseEntity<>("Password successfully changed!", HttpStatus.OK);
        }
        ResponseEntity<Object> entity = new ResponseEntity<>("Something bad happened", HttpStatus.BAD_REQUEST);
        return entity;
    }

    @PutMapping
    public ResponseEntity<DoctorResponseDto> updateDoctorAccount(@RequestParam(name = "email", required = true) String email,
                                                                @RequestBody DoctorUpdateDto doctorUpdateDto)  {
        DoctorResponseDto doctorResponseDto = doctorService.updateAccount(doctorUpdateDto, email);
        if(doctorResponseDto != null) {
            return new ResponseEntity<>(doctorResponseDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
