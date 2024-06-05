package com.example.backend.model.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class TreatmentTakingRequestDto {
    private String patientEmail;
    private Long treatmentId;
    private Date administrationDate;
}
