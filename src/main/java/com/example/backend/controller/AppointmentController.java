package com.example.backend.controller;

import com.example.backend.model.dto.request.AppointmentRequestDto;
import com.example.backend.model.dto.response.AppointmentResponseDto;
import com.example.backend.model.dto.update.AppointmentUpdateDto;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
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

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) {
        return new ResponseEntity<>(appointmentService.getAppointmentById(id), HttpStatus.OK);
    }

    @PutMapping("/cancel/patient/{id}")
    public ResponseEntity<String> patientCancelesAppointment(@PathVariable Long id) {
        appointmentService.patientCancelsAppointment(id);
        return new ResponseEntity<>("Patient canceled appointement, is going to pick a new date", HttpStatus.OK);
    }

    @PutMapping("/cancel/doctor/{id}")
    public ResponseEntity<String> doctorCancelesAppointment(@PathVariable Long id) {
        appointmentService.doctorCancelsAppointment(id);
        return new ResponseEntity<>("Doctor canceled appointement, is going to pick a new date", HttpStatus.OK);
    }

    @PutMapping("/confirm-canceled/doctor/{id}")
    public ResponseEntity<String> doctorConfirms(@PathVariable Long id) {
        appointmentService.doctorConfirms(id);
        return new ResponseEntity<>("Doctor accepted the new date", HttpStatus.OK);
    }

    @PutMapping("/confirm-canceled/patient/{id}")
    public ResponseEntity<String> patientConfirms(@PathVariable Long id) {
        appointmentService.patientConfirms(id);
        return new ResponseEntity<>("Patient accepted the new date", HttpStatus.OK);
    }

    @PutMapping("/canceled/{id}")
    public ResponseEntity<AppointmentResponseDto> changeDateAfterCancelation(@PathVariable Long id, @RequestBody AppointmentUpdateDto appointmentUpdateDto) {
        AppointmentResponseDto result = appointmentService.updateCancelation(id, appointmentUpdateDto);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointmentById(@PathVariable Long id) {
        appointmentService.deleteAppointmentById(id);
        return new ResponseEntity<>("Appointment succesfully deleted", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> getDoctorsAppointments(@RequestParam(name = "email", required = true) String doctorEmail) throws ObjectNotFound {
        List<AppointmentResponseDto> result = appointmentService.getSomeonesAppointments(doctorEmail, "Doctor");
        if (result != null) {
            return new ResponseEntity<List<AppointmentResponseDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/patient")
    public ResponseEntity<List<AppointmentResponseDto>> getPatientsAppointments(@RequestParam(name = "email", required = true) String patientEmail) throws ObjectNotFound {
        List<AppointmentResponseDto> result = appointmentService.getSomeonesAppointments(patientEmail, "Patient");
        if (result != null) {
            return new ResponseEntity<List<AppointmentResponseDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/paged-doctor")
    public ResponseEntity<Page<AppointmentResponseDto>> getDoctorsPagedAppointments(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                    @RequestParam(required = true) int pageSize,
                                                                                    @RequestParam(required = true) int pageNumber,
                                                                                    @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory));
        return new ResponseEntity<>(appointmentService.getPagedAppointments(doctorEmail,"Doctor",pageable), HttpStatus.OK);
    }

    @GetMapping("/paged-patient")
    public ResponseEntity<Page<AppointmentResponseDto>> getPatientsPagedAppointments(@RequestParam(name = "email", required = true) String patientEmail,
                                                                                    @RequestParam(required = true) int pageSize,
                                                                                    @RequestParam(required = true) int pageNumber,
                                                                                    @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory));
        return new ResponseEntity<>(appointmentService.getPagedAppointments(patientEmail,"Patient",pageable), HttpStatus.OK);
    }

    @GetMapping("/patient/chosen-day")
    public ResponseEntity<Page<AppointmentResponseDto>> getPatientsAppointmentsOnACertainDay(@RequestParam(name = "email", required = true) String patientEmail,
                                                                                             @RequestParam(required = true) String date,
                                                                                             @RequestParam(required = true) int pageSize,
                                                                                             @RequestParam(required = true) int pageNumber,
                                                                                             @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory));
        return new ResponseEntity<>(appointmentService.getAppointmentsOnACertainDay(patientEmail, "Patient", date, pageable), HttpStatus.OK);
    }

    @GetMapping("/doctor/chosen-day")
    public ResponseEntity<Page<AppointmentResponseDto>> getDoctorsAppointmentsOnACertainDay(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                            @RequestParam(required = true) String date,
                                                                                            @RequestParam(required = true) int pageSize,
                                                                                            @RequestParam(required = true) int pageNumber,
                                                                                            @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory));
        return new ResponseEntity<>(appointmentService.getAppointmentsOnACertainDay(doctorEmail,"Doctor", date, pageable), HttpStatus.OK);
    }

    @GetMapping("/latest")
    public ResponseEntity<AppointmentResponseDto> getLatestAppointment(@RequestParam(name = "email", required = true) String patientEmail) {
        return new ResponseEntity<>(appointmentService.getPatientsMostRecentPastAppointment(patientEmail), HttpStatus.OK);
    }
}
