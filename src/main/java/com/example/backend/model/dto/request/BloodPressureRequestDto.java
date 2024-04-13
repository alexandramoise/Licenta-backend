package com.example.backend.model.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class BloodPressureRequestDto {
    private Integer systolic;
    private Integer diastolic;
    private Integer pulse;
    private Date date;
}
