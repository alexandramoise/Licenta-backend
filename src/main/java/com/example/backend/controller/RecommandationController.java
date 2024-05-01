package com.example.backend.controller;

import com.example.backend.model.dto.request.RecommandationRequestDto;
import com.example.backend.model.dto.response.RecommandationResponseDto;
import com.example.backend.service.RecommandationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommandations")
public class RecommandationController {
    private final RecommandationService recommandationService;

    public RecommandationController(RecommandationService recommandationService) {
        this.recommandationService = recommandationService;
    }

    @PostMapping
    public ResponseEntity<RecommandationResponseDto> addRecommandation(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                       @RequestBody RecommandationRequestDto recommandationRequestDto) {
        RecommandationResponseDto result = recommandationService.addRecomandation(doctorEmail, recommandationRequestDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<RecommandationResponseDto>> getRecommandations(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                              @RequestParam(required = true) int pageSize,
                                                                              @RequestParam(required = true) int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<RecommandationResponseDto> result = recommandationService.getAllRecommandations(doctorEmail, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/byType")
    public ResponseEntity<Page<RecommandationResponseDto>> getRecommandationsForType(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                     @RequestParam(required = true) int pageSize,
                                                                                     @RequestParam(required = true) int pageNumber,
                                                                                     @RequestParam(required = true) String type) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<RecommandationResponseDto> result = recommandationService.getRecommandationsForType(doctorEmail, type, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/byHashtag")
    public ResponseEntity<Page<RecommandationResponseDto>> getRecommandationsByHashtag(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                     @RequestParam(required = true) int pageSize,
                                                                                     @RequestParam(required = true) int pageNumber,
                                                                                     @RequestParam(required = true) String hashtag) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<RecommandationResponseDto> result = recommandationService.getAllRecommandationsByHashtag(doctorEmail, hashtag, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/byHashtagAndType")
    public ResponseEntity<Page<RecommandationResponseDto>> getRecommandationsByHashtagAndType(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                              @RequestParam(required = true) int pageSize,
                                                                                              @RequestParam(required = true) int pageNumber,
                                                                                              @RequestParam(required = true) String hashtag,
                                                                                              @RequestParam(required = true) String type) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<RecommandationResponseDto> result = recommandationService.getRecommandationsByHashtagForType(doctorEmail, hashtag, type, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/forPatient/all")
    public ResponseEntity<Page<RecommandationResponseDto>> getAllRecommandationsForPatient(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                           @RequestParam(required = true) int pageSize,
                                                                                           @RequestParam(required = true) int pageNumber,
                                                                                           @RequestParam(required = true) String type) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<RecommandationResponseDto> result = recommandationService.getAllRecommandationsForPatient(doctorEmail, type, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/forPatient/byTag")
    public ResponseEntity<Page<RecommandationResponseDto>> getRecommandationsByTagForPatient(@RequestParam(name = "email", required = true) String doctorEmail,
                                                                                             @RequestParam(required = true) int pageSize,
                                                                                             @RequestParam(required = true) int pageNumber,
                                                                                             @RequestParam(required = true) String type,
                                                                                             @RequestParam(required = true) String hashtag) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<RecommandationResponseDto> result = recommandationService.getByTagForPatient(doctorEmail, hashtag, type, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
