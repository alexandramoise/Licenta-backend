package com.example.backend.model.dto.response;

import lombok.Data;

@Data
public class RecommandationResponseDto {
    private Long id;
    private String text;
    private String hashtag;
    private String recommandationType;
    private String doctorEmail;
}
