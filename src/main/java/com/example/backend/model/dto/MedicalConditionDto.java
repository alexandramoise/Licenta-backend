package com.example.backend.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MedicalConditionDto {
    private String name;
    private Date startingDate;
    private Date endingDate;
}
