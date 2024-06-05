package com.example.backend.model.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class TreatmentTakingResponseDto {
    private Long id;
    private String patientEmail;
    private Long treatmentId;
    private Date administrationDate;
}
