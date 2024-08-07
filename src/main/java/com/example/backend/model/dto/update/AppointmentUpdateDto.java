package com.example.backend.model.dto.update;

import lombok.Data;

import java.util.Date;

@Data
public class AppointmentUpdateDto {
    private Date date;
    private String visitType;
    private String comment;
}
