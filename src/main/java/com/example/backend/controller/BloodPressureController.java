package com.example.backend.controller;

import com.example.backend.model.dto.BloodPressureRequestDto;
import com.example.backend.model.dto.BloodPressureResponseDto;
import com.example.backend.service.BloodPressureService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bloodPressures")
@Log4j2
public class BloodPressureController {
    private final BloodPressureService bloodPressureService;

    public BloodPressureController(BloodPressureService bloodPressureService) {
        this.bloodPressureService = bloodPressureService;
    }

    @PostMapping
    public ResponseEntity<BloodPressureResponseDto> addBloodPressure(@RequestBody BloodPressureRequestDto bloodPressureRequestDto,
                                                                     @RequestParam(name = "email") String patientEmail) {
        BloodPressureResponseDto result = bloodPressureService.addBloodPressure(bloodPressureRequestDto, patientEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping()
    public ResponseEntity<List<BloodPressureResponseDto>> getPatientBPs(@RequestParam(name = "email") String patientEmail) {
        List<BloodPressureResponseDto> result = bloodPressureService.getPatientBloodPressures(patientEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BloodPressureResponseDto> updateBPbyId(@PathVariable Long id, @RequestBody BloodPressureRequestDto bloodPressureRequestDto) {
        BloodPressureResponseDto result = bloodPressureService.updateBloodPressureById(id, bloodPressureRequestDto);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBPbyId(@PathVariable Long id) {
        bloodPressureService.deleteBloodPressureById(id);
        return new ResponseEntity<>("Blood pressure succesfully deleted", HttpStatus.OK);
    }
}
