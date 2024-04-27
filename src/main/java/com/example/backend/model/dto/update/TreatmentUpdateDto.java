package com.example.backend.model.dto.update;

import lombok.Data;

@Data
public class TreatmentUpdateDto {
    private String medicineName;
    private Integer doses;
    private String comment;
}
