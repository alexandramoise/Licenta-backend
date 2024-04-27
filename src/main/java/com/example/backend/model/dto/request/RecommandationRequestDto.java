package com.example.backend.model.dto.request;

import lombok.Data;

@Data
public class RecommandationRequestDto {
    private String text;
    private String hashtag;
    private String recommandationType;
}
