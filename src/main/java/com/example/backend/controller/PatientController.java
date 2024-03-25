package com.example.backend.controller;

import com.example.backend.model.dto.*;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.service.PatientService;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/patients")
public class PatientController {
    private final UserService userService;
    private final PatientService patientService;
    private final SendEmailService sendEmailService;

    public PatientController(UserService userService, PatientService patientService, SendEmailService sendEmailService) {
        this.userService = userService;
        this.patientService = patientService;
        this.sendEmailService = sendEmailService;
    }

    @Transactional
    @PostMapping("/{doctorId}")
    public ResponseEntity<PatientResponseDto> initiatePatientAccount(@RequestParam(name = "email", required = true) String email, @PathVariable Long doctorId) throws UnsupportedEncodingException {
        PatientResponseDto patient = patientService.createAccount(email, doctorId);
        log.info("In DoctorController: trimit - " + patient.getEmail());
        if (patient != null) {
            return new ResponseEntity<PatientResponseDto>(patient, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping
    public ResponseEntity<PatientResponseDto> updatePatientAccount(@RequestParam(name = "email", required = true) String email,
                                                                 @RequestBody PatientUpdateDto patientUpdateDto)  {
        PatientResponseDto patientResponseDto = patientService.updateAccount(patientUpdateDto);
        if(patientResponseDto != null) {
            return new ResponseEntity<>(patientResponseDto, HttpStatus.OK);
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

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable Long id) {
        PatientResponseDto result = patientService.getPatientById(id);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PatientResponseDto>> getAllPatients(@RequestParam(name = "email", required = true) String doctorEmail) throws ObjectNotFound {
        List<PatientResponseDto> result = patientService.getAllPatients(doctorEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/medical-conditions/{id}")
    public ResponseEntity<List<String>> getPatientMedicalConditions(@PathVariable Long id) {
        List<String> result = patientService.getPatientsMedicalConditions(id);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
