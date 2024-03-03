package com.example.backend.controller;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.dto.DoctorResponseDto;
import com.example.backend.model.dto.DoctorUpdateDto;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/doctors")
@Log4j2
public class DoctorController {
    private final UserService userService;
    private final SendEmailService sendEmailService;

    public DoctorController(UserService userService, SendEmailService sendEmailService) {
        this.userService = userService;
        this.sendEmailService = sendEmailService;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<DoctorResponseDto> doctorInitiatesAccount(@RequestParam(name = "email", required = true) String email) throws UnsupportedEncodingException {
        DoctorResponseDto doctor = userService.createAccount(email, "Doctor", Long.valueOf(0));
        log.info("In DoctorController: trimit - " + doctor.getEmail());
        if (doctor != null) {
            return new ResponseEntity<DoctorResponseDto>(doctor, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PutMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordDto changePasswordDto) throws IllegalAccessException {
        boolean passwordChanged = userService.changePassword(changePasswordDto, "Doctor");
        if (passwordChanged) {
            return new ResponseEntity<>("Password successfully changed!", HttpStatus.OK);
        }
        ResponseEntity<Object> entity = new ResponseEntity<>("Something bad happened", HttpStatus.BAD_REQUEST);
        return entity;
    }

    @Transactional
    @PutMapping("/set-name")
    public ResponseEntity<Object> setName(@RequestBody DoctorUpdateDto doctorUpdateDto) {
        //doctorService.updateDoctor(doctorUpdateDto);
        return new ResponseEntity<>("Updated successfully!", HttpStatus.OK);
    }
}
