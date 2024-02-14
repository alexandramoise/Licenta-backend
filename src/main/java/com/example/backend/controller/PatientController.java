package com.example.backend.controller;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Log4j2
@RestController
@RequestMapping("/patients")
public class PatientController {
    private final UserService userService;
    private final SendEmailService sendEmailService;

    public PatientController(UserService userService, SendEmailService sendEmailService) {
        this.userService = userService;
        this.sendEmailService = sendEmailService;
    }

    @Transactional
    @PostMapping("/add-patient")
    public ResponseEntity<PatientResponseDto> initiatePatientAccount(@RequestParam(name = "email") String email) throws UnsupportedEncodingException {
        PatientResponseDto patient = userService.createAccount(email, "Patient");
        log.info("In DoctorController: trimit - " + patient.getEmail());
        if (patient != null) {
            return new ResponseEntity<PatientResponseDto>(patient, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PutMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordDto changePasswordDto) throws IllegalAccessException {
        boolean passwordChanged = userService.changePassword(changePasswordDto, "Patient");
        if (passwordChanged) {
            return new ResponseEntity<>("Password successfully changed!", HttpStatus.OK);
        }
        ResponseEntity<Object> entity = new ResponseEntity<>("Something bad happened", HttpStatus.BAD_REQUEST);
        return entity;
    }
}
