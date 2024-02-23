package com.example.backend.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AppointmentUpdateDto {
    private Date date;
    private String visitType;
}
