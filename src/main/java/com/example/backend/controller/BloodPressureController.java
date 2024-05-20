package com.example.backend.controller;

import com.example.backend.model.dto.request.BloodPressureRequestDto;
import com.example.backend.model.dto.response.BloodPressureResponseDto;
import com.example.backend.model.entity.BloodPressureType;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.service.BloodPressureService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bloodPressures")
@Log4j2
public class BloodPressureController {
    private final BloodPressureService bloodPressureService;

    public BloodPressureController(BloodPressureService bloodPressureService) {
        this.bloodPressureService = bloodPressureService;
    }

    @PostMapping
    public ResponseEntity<BloodPressureResponseDto> addBloodPressure(@RequestBody BloodPressureRequestDto bloodPressureRequestDto,
                                                                     @RequestParam(name = "email", required = true) String patientEmail) {
        BloodPressureResponseDto result = bloodPressureService.addBloodPressure(bloodPressureRequestDto, patientEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<BloodPressureResponseDto>> getPatientBPs(@RequestParam(name = "email", required = true) String patientEmail) {
        List<BloodPressureResponseDto> result = bloodPressureService.getPatientBloodPressures(patientEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/byDate")
    public ResponseEntity<List<BloodPressureResponseDto>> getPatientBPsInTime(@RequestParam(name = "email") String patientEmail,
                                                                              @RequestParam(name = "fromDate") String fromDate,
                                                                              @RequestParam(name = "toDate") String toDate) {
        List<BloodPressureResponseDto> result = bloodPressureService.getPatientBPsByTime(patientEmail, fromDate, toDate);
        log.info("DE LA in controller: " + fromDate + " la " + toDate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodPressureResponseDto> getBpById(@RequestParam(name = "email", required = true) String patientEmail,
                                               @PathVariable Long id) {
        BloodPressureResponseDto result = bloodPressureService.getBloodPressureById(id, patientEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<BloodPressureResponseDto>> getPagedBPs(@RequestParam(name = "email", required = true) String patientEmail,
                                                                        @RequestParam(required = true) int pageSize,
                                                                        @RequestParam(required = true) int pageNumber,
                                                                        @RequestParam(required = true) String sortCategory) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory));
        return new ResponseEntity<>(bloodPressureService.getPagedBloodPressures(patientEmail, pageable), HttpStatus.OK);

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

    @GetMapping("/tendencyOverTime")
    public ResponseEntity<Map<Date, BloodPressureType>> getBPTendency(@RequestParam(name = "email", required = true) String patientEmail) throws ObjectNotFound {
        Map<Date, BloodPressureType> result = bloodPressureService.getPatientBPTendencyOverTime(patientEmail);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
