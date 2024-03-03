package com.example.backend.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AppointmentRequestDto {
    private Date date;
    private Long doctorId;
    private Long patientId;
    private String visitType;
    private Boolean patientIsComing;
    private Boolean doctorIsAvailable;
}
