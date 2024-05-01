package com.example.backend.controller;

import com.example.backend.model.dto.request.TreatmentRequestDto;
import com.example.backend.model.dto.response.TreatmentResponseDto;
import com.example.backend.model.dto.update.TreatmentUpdateDto;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.service.TreatmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {
    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @PostMapping
    public ResponseEntity<TreatmentResponseDto> addTreatment(@RequestBody TreatmentRequestDto treatmentRequestDto) throws ObjectNotFound {
        TreatmentResponseDto result = treatmentService.addTreatment(treatmentRequestDto);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentResponseDto> updateTreatment(@PathVariable Long id,
                                                                @RequestBody TreatmentUpdateDto treatmentUpdateDto) throws ObjectNotFound {
        TreatmentResponseDto result = treatmentService.updateTreatment(id, treatmentUpdateDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TreatmentResponseDto> endTreatment(@PathVariable Long id) throws ObjectNotFound {
        TreatmentResponseDto result = treatmentService.markAsEnded(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<TreatmentResponseDto>> getPatientTreatments(@RequestParam(name = "email", required = true) String email,
                                                                           @RequestParam(name = "medicalCondition", required = true) String medicalCondition) {
        List <TreatmentResponseDto> result = treatmentService.getPatientTreatments(email, medicalCondition);
        if(result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<TreatmentResponseDto>> getAllPagedTreatmentsForPatientCondition(@RequestParam(name = "patientEmail") String patientEmail,
                                                                                               @RequestParam(name = "medicalConditionName") String medicalConditionName,
                                                                                               @RequestParam(name = "pageSize") int pageSize,
                                                                                               @RequestParam(name = "pageNumber") int pageNumber,
                                                                                               @RequestParam(name = "sortCategory") String sortCategory) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory));
        Page<TreatmentResponseDto> treatments = treatmentService.getPagedTreatments(patientEmail, medicalConditionName, pageable);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }

}
