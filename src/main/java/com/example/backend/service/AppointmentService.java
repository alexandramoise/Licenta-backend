package com.example.backend.service;

import com.example.backend.model.dto.request.AppointmentRequestDto;
import com.example.backend.model.dto.response.AppointmentResponseDto;
import com.example.backend.model.dto.update.AppointmentUpdateDto;
import com.example.backend.model.entity.table.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface AppointmentService {
    AppointmentResponseDto createAppointment(AppointmentRequestDto appointmentRequestDto);
    AppointmentResponseDto updateAppointmentById(Long id, AppointmentUpdateDto appointmentUpdateDto);
    AppointmentResponseDto getAppointmentById(Long id);
    AppointmentResponseDto updateCancelation(Long id, AppointmentUpdateDto appointmentUpdateDto);

    void patientCancelsAppointment(Long id);
    void doctorCancelsAppointment(Long id);
    void patientConfirms(Long id);
    void doctorConfirms(Long id);

    void deleteAppointmentById(Long id);

    List<AppointmentResponseDto> getSomeonesAppointments(String email, String role);
    List<AppointmentResponseDto> getByTimeInterval(String patientEmail, String fromDate, String toDate);
    AppointmentResponseDto getPatientsMostRecentPastAppointment(String email);

    Page<AppointmentResponseDto> getPagedAppointments(String email, String role, Pageable pageable);
    Page<AppointmentResponseDto> getAppointmentsOnACertainDay(String email, String role, String date, Pageable pageable);
    boolean checkTimeIsAvailable(List<Appointment> appointments, Date date, String visitType);
    boolean isSameDay(Date d1, Date d2);

    Date addMinutes(Date date, int minutes);
    Date subtractMinutes(Date date, int minutes);
}
