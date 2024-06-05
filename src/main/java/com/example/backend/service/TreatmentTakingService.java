package com.example.backend.service;

import com.example.backend.model.dto.request.TreatmentTakingRequestDto;
import com.example.backend.model.dto.response.TreatmentTakingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TreatmentTakingService {
    TreatmentTakingResponseDto addTreatmentTaking(TreatmentTakingRequestDto treatmentTakingRequestDto);
    List<TreatmentTakingResponseDto> getTreatmentTakings(Long treatmentId, String patientEmail, String date);

    void checkAndSendAlert(Long treatmentId, String patientEmail);

    void scheduledChecking();
}
