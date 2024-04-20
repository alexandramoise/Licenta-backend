package com.example.backend.service;

import com.example.backend.model.dto.request.RecommandationRequestDto;
import com.example.backend.model.dto.response.RecommandationResponseDto;
import com.example.backend.model.entity.BloodPressureType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface RecommandationService {
    RecommandationResponseDto addRecomandation(String doctorEmail, RecommandationRequestDto recommandationRequestDto);
    Page<RecommandationResponseDto> getAllRecommandations(String doctorEmail, Pageable pageable);
    Page<RecommandationResponseDto> getAllRecommandationsByHashtag(String doctorEmail, String hashtag, Pageable pageable);
    Page<RecommandationResponseDto> getRecommandationsForType(String doctorEmail, String recommandationType, Pageable pageable);
    Page<RecommandationResponseDto> getRecommandationsByHashtagForType(String doctorEmail, String hashtag, String recommandationType, Pageable pageable);

    Page<RecommandationResponseDto> getAllRecommandationsForPatient(String doctorEmail, String type, Pageable pageable);
    Page<RecommandationResponseDto> getByTagForPatient(String doctorEmail, String hashtag, String type, Pageable pageable);
}
