package com.example.backend.controller;

import com.example.backend.model.dto.AppointmentRequestDto;
import com.example.backend.model.dto.AppointmentResponseDto;
import com.example.backend.model.dto.AppointmentUpdateDto;
import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@RequestBody AppointmentRequestDto appointmentRequestDto) throws ObjectNotFound {
        AppointmentResponseDto result = appointmentService.createAppointment(appointmentRequestDto);
        if (result != null) {
            return new ResponseEntity<AppointmentResponseDto>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(@PathVariable Long id, @RequestBody AppointmentUpdateDto appointmentUpdateDto) throws ObjectNotFound {
        AppointmentResponseDto result = appointmentService.updateAppointmentById(id, appointmentUpdateDto);
        if (result != null) {
            return new ResponseEntity<AppointmentResponseDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBPbyId(@PathVariable Long id) {
        appointmentService.deleteAppointmentById(id);
        return new ResponseEntity<>("Appointment succesfully deleted", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> getDoctorsAppointments(@RequestParam(name = "email", required = true) String doctorEmail) throws ObjectNotFound {
        List<AppointmentResponseDto> result = appointmentService.getSomeonesAppointments(doctorEmail, "Doctor");
        if (result != null) {
            return new ResponseEntity<List<AppointmentResponseDto>>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/patient")
    public ResponseEntity<List<AppointmentResponseDto>> getPatientsAppointments(@RequestParam(name = "email", required = true) String patientEmail) throws ObjectNotFound {
        List<AppointmentResponseDto> result = appointmentService.getSomeonesAppointments(patientEmail, "Patient");
        if (result != null) {
            return new ResponseEntity<List<AppointmentResponseDto>>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
