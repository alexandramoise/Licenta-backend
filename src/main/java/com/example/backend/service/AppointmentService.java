package com.example.backend.service;

import com.example.backend.model.dto.AppointmentRequestDto;
import com.example.backend.model.dto.AppointmentResponseDto;
import com.example.backend.model.dto.AppointmentUpdateDto;
import com.example.backend.model.entity.Appointment;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface AppointmentService {
    AppointmentResponseDto createAppointment(AppointmentRequestDto appointmentRequestDto);
    AppointmentResponseDto updateAppointmentById(Long id, AppointmentUpdateDto appointmentUpdateDto);
    AppointmentResponseDto updateCancelation(Long id, AppointmentUpdateDto appointmentUpdateDto);

    void patientCancelsAppointment(Long id);
    void doctorCancelsAppointment(Long id);
    void patientConfirms(Long id);
    void doctorConfirms(Long id);

    void deleteAppointmentById(Long id);

    List<AppointmentResponseDto> getSomeonesAppointments(String email, String role);
    boolean checkTimeIsAvailable(List<Appointment> appointments, Date date);
    boolean isSameDay(Date d1, Date d2);

    Date add30Minutes(Date date);
    Date subtract30Minutes(Date date);
}
