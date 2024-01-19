package com.example.backend.controller;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.dto.DoctorUpdateDto;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    private final UserService userService;
    private final SendEmailService sendEmailService;

    public DoctorController(UserService userService, SendEmailService sendEmailService) {
        this.userService = userService;
        this.sendEmailService = sendEmailService;
    }

    @Transactional
    @PostMapping("/new-doctor-account")
    public ResponseEntity<String> doctorInitiatesAccount(@RequestParam(name = "email") String email) throws IllegalAccessException {
        //doctorService.createDoctorAccount(email);
        userService.createAccount(email, "Doctor");
        return new ResponseEntity<>("Account initiated successfully!", HttpStatus.CREATED);
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
