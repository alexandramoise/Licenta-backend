package com.example.backend.model.dto.request;

import com.example.backend.model.entity.BloodPressureType;
import lombok.Data;

@Data
public class RecommandationRequestDto {
    private String text;
    private String hashtag;
    private String recommandationType;
}
