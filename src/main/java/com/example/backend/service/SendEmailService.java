package com.example.backend.service;

import com.example.backend.model.entity.table.Appointment;
import com.example.backend.model.entity.table.Treatment;
import org.springframework.stereotype.Service;

@Service
public interface SendEmailService {
     <T> T sendCreateAccountEmail(String email, String accountType);
    void sendResetPasswordEmail(String email, String accountType);
    void sendCreateAppointmentEmail(Appointment appointment, Long id);
    void sendUpdateAppointmentEmail(Appointment appointment, Long id);
    void sendAppointmentReminder(Appointment appointment, Long id);

    void sendTreatmentAdministrationReminder(Long treatmentId, String email);
    void sendTreatmentAdded(Long treatmentId, String email);
    void sendTreatmentChanged(Long treatmentId, String email);
    void sendEndedTreatment(Long treatmentId, String email);

    void sendPatientChangedType(Long patientId, String newTendency);
}
