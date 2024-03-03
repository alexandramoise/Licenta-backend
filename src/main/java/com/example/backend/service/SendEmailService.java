package com.example.backend.service;

import com.example.backend.model.entity.Appointment;
import org.springframework.stereotype.Service;

@Service
public interface SendEmailService {
     <T> T sendCreateAccountEmail(String email, String accountType);
    void sendCreateAppointmentEmail(Appointment appointment, Long id);
    void sendUpdateAppointmentEmail(Appointment appointment, Long id);
}
