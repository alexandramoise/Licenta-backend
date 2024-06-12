package com.example.backend.model.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class TreatmentResponseDto {
    private Long id;
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String medicalConditionName;
    private String medicineName;
    private Integer doses;
    private String comment;
    private Date startingDate;
    private Date endingDate;
//  private List<TreatmentTaking> treatmentTakings;
}
