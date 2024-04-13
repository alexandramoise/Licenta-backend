package com.example.backend.model.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class AppointmentRequestDto {
    private Date date;
    private String doctorEmail;
    private String patientEmail;
    private String visitType;
    private Boolean patientIsComing;
    private Boolean doctorIsAvailable;
}
