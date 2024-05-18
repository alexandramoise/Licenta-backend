package com.example.backend.controller;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.update.PatientUpdateDto;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.InvalidCredentials;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.PatientService;
import com.example.backend.service.SendEmailService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;
    private final SendEmailService sendEmailService;

    private final PasswordEncoder passwordEncoder;
    private final PatientRepo patientRepo;

    public PatientController(PatientService patientService, SendEmailService sendEmailService, PasswordEncoder passwordEncoder, PatientRepo patientRepo) {
        this.patientService = patientService;
        this.sendEmailService = sendEmailService;
        this.passwordEncoder = passwordEncoder;
        this.patientRepo = patientRepo;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<PatientResponseDto> initiatePatientAccount(@RequestParam(name = "email", required = true) String email,
                                                                     @RequestParam(name = "doctorEmail", required = true) String doctorEmail) throws UnsupportedEncodingException {
        PatientResponseDto patient = patientService.createAccount(email, doctorEmail);
        log.info("IN METODA DIN CONTROLLEEEER");
        log.info("In DoctorController: trimit - " + patient.getEmail());
        if (patient != null) {
            return new ResponseEntity<PatientResponseDto>(patient, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/first-login")
    public ResponseEntity<Boolean> getFirstLogin(@RequestParam(name = "email", required = true) String email) {
        return new ResponseEntity<>(patientService.getFirstLoginEver(email), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<PatientResponseDto> updatePatientAccount(@RequestParam(name = "email", required = true) String email,
                                                                 @RequestBody PatientUpdateDto patientUpdateDto)  {
        PatientResponseDto patientResponseDto = patientService.updateAccount(patientUpdateDto, email);
        if(patientResponseDto != null) {
            return new ResponseEntity<>(patientResponseDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PutMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordDto changePasswordDto) throws ObjectNotFound, InvalidCredentials {
        boolean passwordChanged = patientService.changePassword(changePasswordDto);
        if (passwordChanged) {
            return new ResponseEntity<>("Password successfully changed!", HttpStatus.OK);
        }
        ResponseEntity<Object> entity = new ResponseEntity<>("Something bad happened", HttpStatus.BAD_REQUEST);
        return entity;
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestNewPassword(@RequestParam(required = true) String email) throws ObjectNotFound {
        patientService.requestPasswordChange(email);
        return new ResponseEntity<>("Request received, check your email", HttpStatus.OK);
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

    @GetMapping
    public ResponseEntity<PatientResponseDto> getPatientByEmail(@RequestParam(name = "email", required = true) String email) throws ObjectNotFound {
        PatientResponseDto result = patientService.getPatientByEmail(email);
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



    @GetMapping("/filtered/paged")
    public ResponseEntity<Page<PatientResponseDto>> getPagedFilteredPatients(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                             @RequestParam(name = "name", required = false) String name,
                                                                             @RequestParam(name = "gender", required = false) String gender,
                                                                             @RequestParam(name = "maxAge", required = false) Integer maxAge,
                                                                             @RequestParam(name = "type", required = false) String type,
                                                                             @RequestParam(required = true) int pageSize,
                                                                             @RequestParam(required = true) int pageNumber,
                                                                             @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, sortCategory));
        return new ResponseEntity<>(patientService.getFilteredPagedPatients(doctorEmail, name, gender, maxAge, type, pageable), HttpStatus.OK);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<PatientResponseDto>> getAllPagedPatients(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                        @RequestParam(required = true) int pageSize,
                                                                        @RequestParam(required = true) int pageNumber,
                                                                        @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, sortCategory));
        return new ResponseEntity<>(patientService.getAllPagedPatients(doctorEmail, pageable), HttpStatus.OK);

    }

    @GetMapping("/medical-conditions/{id}")
    public ResponseEntity<List<MedicalConditionDto>> getPatientMedicalConditions(@PathVariable Long id) {
        List<MedicalConditionDto> result = patientService.getPatientsMedicalConditions(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestBody String password) {
        Patient patient = patientRepo.findByEmail(email).get();
        String encodedPassword = patient.getPassword();

        boolean isPwdRight = passwordEncoder.matches(password, encodedPassword);
        if(isPwdRight) {
            return new ResponseEntity<>("DA BRAVO", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Upsi...", HttpStatus.BAD_REQUEST);
        }
    }
}
