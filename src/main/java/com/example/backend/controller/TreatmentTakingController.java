package com.example.backend.controller;

import com.example.backend.model.dto.request.TreatmentTakingRequestDto;
import com.example.backend.model.dto.response.TreatmentTakingResponseDto;
import com.example.backend.service.TreatmentTakingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatmentTakings")
public class TreatmentTakingController {
    private final TreatmentTakingService treatmentTakingService;

    public TreatmentTakingController(TreatmentTakingService treatmentTakingService) {
        this.treatmentTakingService = treatmentTakingService;
    }

    @PostMapping
    public ResponseEntity<TreatmentTakingResponseDto> addTreatmentTaking(@RequestBody TreatmentTakingRequestDto treatmentTakingRequestDto) {
        return new ResponseEntity<>(treatmentTakingService.addTreatmentTaking(treatmentTakingRequestDto), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<TreatmentTakingResponseDto>> getPatientsTreatmentTakings(@RequestParam(name = "email") String patientEmail,
                                                                                        @RequestParam(name = "treatmentId") Long treatmentId,
                                                                                        @RequestParam(name = "date") String date) {
        return new ResponseEntity<>(treatmentTakingService.getTreatmentTakings(treatmentId, patientEmail, date), HttpStatus.OK);
    }
}
