package com.example.backend.model.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class AppointmentResponseDto {
    private Long id;
    private Date date;
    private String doctorEmail;
    private String patientEmail;
    private String visitType;
    private String comment;
    private Boolean nobodyCanceled;
}
