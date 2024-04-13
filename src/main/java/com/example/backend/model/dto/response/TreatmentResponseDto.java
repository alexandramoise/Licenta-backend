package com.example.backend.model.dto.response;

import com.example.backend.model.entity.TreatmentTaking;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TreatmentResponseDto {
    private Long id;
    private Long patientId;
    private String medicalConditionName;
    private String medicineName;
    private Integer doses;
    private List<TreatmentTaking> treatmentTakings;
    private Date startingDate;
    private Date endingDate;
}
