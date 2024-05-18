package com.example.backend.service.implementation;

import com.example.backend.model.dto.request.AppointmentRequestDto;
import com.example.backend.model.dto.response.AppointmentResponseDto;
import com.example.backend.model.dto.update.AppointmentUpdateDto;
import com.example.backend.model.entity.table.Appointment;
import com.example.backend.model.entity.table.Doctor;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.EmptyList;
import com.example.backend.model.exception.InvalidAccountType;
import com.example.backend.model.exception.InvalidValues;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.AppointmentRepo;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.AppointmentService;
import com.example.backend.service.SendEmailService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Log4j2
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepo appointmentRepo;
    private final SendEmailService sendEmailService;
    private final ModelMapper modelMapper;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public AppointmentServiceImpl(AppointmentRepo appointmentRepo, SendEmailService sendEmailService, ModelMapper modelMapper,
                                  DoctorRepo doctorRepo, PatientRepo patientRepo) {
        this.appointmentRepo = appointmentRepo;
        this.sendEmailService = sendEmailService;
        this.modelMapper = modelMapper;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    @Override
    public AppointmentResponseDto createAppointment(AppointmentRequestDto appointmentRequestDto) {
        Patient patient =  patientRepo.findByEmail(appointmentRequestDto.getPatientEmail()).orElseThrow(() -> new ObjectNotFound("Patient not found"));
        Doctor doctor =  doctorRepo.findByEmail(appointmentRequestDto.getDoctorEmail()).orElseThrow(() -> new ObjectNotFound("Doctor not found"));

        Date today = new Date();
        if(appointmentRequestDto.getDate().before(today)) {
            throw new InvalidValues("Date can not be in the past");
        }

        if(!checkTimeIsAvailable(doctor.getAppointments(), appointmentRequestDto.getDate(), appointmentRequestDto.getVisitType())) {
            throw new InvalidValues("Doctor is not available at that time");
        }

        Appointment appointment = new Appointment();
        appointment.setVisitType(appointmentRequestDto.getVisitType());
        appointment.setTime(appointmentRequestDto.getDate());
        appointment.setComment(appointmentRequestDto.getComment());
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setPatientIsComing(true);
        appointment.setDoctorIsAvailable(true);
        appointmentRepo.save(appointment);
        sendEmailService.sendCreateAppointmentEmail(appointment, appointment.getAppointment_id());

        AppointmentResponseDto result = modelMapper.map(appointment, AppointmentResponseDto.class);
        result.setDate(appointment.getTime());
        result.setId(appointment.getAppointment_id());
        result.setDoctorEmail(doctor.getEmail());
        result.setPatientEmail(patient.getEmail());
        result.setNobodyCanceled(appointment.getPatientIsComing() && appointment.getDoctorIsAvailable());
        return result;
    }

    @Override
    public AppointmentResponseDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();

        AppointmentResponseDto result = modelMapper.map(appointment, AppointmentResponseDto.class);
        result.setDate(appointment.getTime());
        result.setId(appointment.getAppointment_id());
        result.setDoctorEmail(doctor.getEmail());
        result.setPatientEmail(patient.getEmail());
        result.setNobodyCanceled(appointment.getPatientIsComing() && appointment.getDoctorIsAvailable());
        return result;
    }

    @Override
    public AppointmentResponseDto updateAppointmentById(Long id, AppointmentUpdateDto appointmentUpdateDto) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        Doctor doctor = appointment.getDoctor();
        List<Appointment> appointments = doctor.getAppointments();
        appointments.remove(appointment); // ca de ex sa pot sa pun programarea cu 10 minute mai devreme,
                                          // daca ar ramane in lista mi-ar zice ca ora nu e disponibila.

        Date today = new Date();
        if(appointmentUpdateDto.getDate().before(today)) {
            throw new InvalidValues("Date can not be in the past");
        }

        if(!checkTimeIsAvailable(appointments, appointmentUpdateDto.getDate(), appointmentUpdateDto.getVisitType())) {
            throw new InvalidValues("Doctor is not available at that time");
        }

        appointment.setTime(appointmentUpdateDto.getDate());
        appointment.setVisitType(appointmentUpdateDto.getVisitType());
        appointment.setComment(appointmentUpdateDto.getComment());
        appointmentRepo.save(appointment);

        AppointmentResponseDto result = modelMapper.map(appointment, AppointmentResponseDto.class);
        result.setDate(appointment.getTime());
        result.setId(appointment.getAppointment_id());
        result.setDoctorEmail(doctor.getEmail());
        result.setPatientEmail(appointment.getPatient().getEmail());
        result.setNobodyCanceled(appointment.getPatientIsComing() && appointment.getDoctorIsAvailable());
        return result;
    }

    @Override
    public AppointmentResponseDto updateCancelation(Long id, AppointmentUpdateDto appointmentUpdateDto) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        boolean patientCanceled = !appointment.getPatientIsComing();
        boolean doctorCanceled = !appointment.getDoctorIsAvailable();
        AppointmentResponseDto result = new AppointmentResponseDto();

        if(doctorCanceled) {
            appointment.setDoctorIsAvailable(true);
            appointment.setPatientIsComing(false); // astept confirmarea
            result = updateAppointmentById(id, appointmentUpdateDto);
            sendEmailService.sendUpdateAppointmentEmail(appointment, appointment.getAppointment_id());
            log.info("Trimit mail PACIENTULUI cu data noua: " + result.getDate());
        } else if(patientCanceled) {
            appointment.setPatientIsComing(true);
            appointment.setDoctorIsAvailable(false); // astept confirmarea
            result = updateAppointmentById(id, appointmentUpdateDto);
            sendEmailService.sendUpdateAppointmentEmail(appointment, appointment.getAppointment_id());
            log.info("Trimit mail DOCTORULUI cu data noua: " + result.getDate());
        } else {
            throw new InvalidValues("Not a cancelation");
        }

        return result;
    }

    @Override
    public void patientCancelsAppointment(Long id) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        appointment.setPatientIsComing(false);
        appointmentRepo.save(appointment);
    }

    @Override
    public void doctorCancelsAppointment(Long id) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        appointment.setDoctorIsAvailable(false);
        appointmentRepo.save(appointment);
    }

    @Override
    public void patientConfirms(Long id) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        appointment.setPatientIsComing(true);
        appointmentRepo.save(appointment);
    }

    @Override
    public void doctorConfirms(Long id) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(() -> new ObjectNotFound("Appointment not found"));
        appointment.setDoctorIsAvailable(true);
        appointmentRepo.save(appointment);
    }

    @Override
    public void deleteAppointmentById(Long id) {
        if(appointmentRepo.existsById(id)) {
            appointmentRepo.deleteById(id);
        } else {
            throw new ObjectNotFound("Appointment Not found");
        }
    }

    @Override
    public List<AppointmentResponseDto> getSomeonesAppointments(String email, String role) {
        if(!(role.equals("Doctor") || role.equals("Patient"))) {
            throw new InvalidAccountType("Invalid account type");
        }

        List<Appointment> appointments = new ArrayList<>();
        if(role.equals("Doctor")) {
            Doctor doctor = doctorRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No doctor account with this email address"));
            appointments = doctor.getAppointments();
        } else {
            Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No patient account with this email address"));
            appointments = patient.getAppointments();
        }

        appointments.sort(Comparator.comparing(Appointment::getTime).reversed());
        List<AppointmentResponseDto> result = appointments
            .stream()
            .map(a -> {
                AppointmentResponseDto aDto = modelMapper.map(a, AppointmentResponseDto.class);
                aDto.setDate(a.getTime());
                aDto.setId(a.getAppointment_id());
                aDto.setDoctorEmail(a.getDoctor().getEmail());
                aDto.setPatientEmail(a.getPatient().getEmail());
                aDto.setNobodyCanceled(a.getPatientIsComing() && a.getDoctorIsAvailable());
                return aDto;
            }).toList();

        return result;
    }

    @Override
    public AppointmentResponseDto getPatientsMostRecentPastAppointment(String email) {
       Patient patient = patientRepo.findByEmail(email).orElseThrow(() -> new ObjectNotFound("No patient with this email address"));

        if(patient.getAppointments().size() == 0 || patient.getAppointments() == null) {
            throw new EmptyList("No appointments");
        }

        Appointment a = appointmentRepo.findLatestOfPatientsAppointments(email);
        if(a == null) {
            throw new InvalidValues("No past appointments");
        }

        AppointmentResponseDto aDto = modelMapper.map(a, AppointmentResponseDto.class);
        aDto.setDate(a.getTime());
        aDto.setId(a.getAppointment_id());
        aDto.setDoctorEmail(a.getDoctor().getEmail());
        aDto.setPatientEmail(a.getPatient().getEmail());
        aDto.setNobodyCanceled(a.getPatientIsComing() && a.getDoctorIsAvailable());
        return aDto;
    }

    @Override
    public Page<AppointmentResponseDto> getPagedAppointments(String email, String role, Pageable pageable) {
        if(!(role.equals("Doctor") || role.equals("Patient"))) {
            throw new InvalidAccountType("Invalid account type");
        }

        Page<Appointment> appointments;
        if(role.equals("Doctor")) {
            if(!doctorRepo.findByEmail(email).isPresent())
                throw new ObjectNotFound("Doctor not found");
            appointments = appointmentRepo.findByDoctorEmail(email, pageable);
        } else {
            if(!patientRepo.findByEmail(email).isPresent())
                throw new ObjectNotFound("Patient not found");
            appointments = appointmentRepo.findByPatientEmail(email, pageable);;
        }

        //appointments.sort(Comparator.comparing(Appointment::getTime).reversed());
        List<AppointmentResponseDto> result =
            appointments
                .getContent()
                .stream()
                .map(a -> {
                    AppointmentResponseDto aDto = modelMapper.map(a, AppointmentResponseDto.class);
                    aDto.setDate(a.getTime());
                    aDto.setId(a.getAppointment_id());
                    aDto.setDoctorEmail(a.getDoctor().getEmail());
                    aDto.setPatientEmail(a.getPatient().getEmail());
                    aDto.setNobodyCanceled(a.getPatientIsComing() && a.getDoctorIsAvailable());
                    return aDto;
                }).toList();

        return new PageImpl<>(result, pageable, appointments.getTotalElements());
    }

    @Override
    public Page<AppointmentResponseDto> getAppointmentsOnACertainDay(String email, String role, String date, Pageable pageable) {
        if(!(role.equals("Doctor") || role.equals("Patient"))) {
            throw new InvalidAccountType("Invalid account type");
        }

        Page<Appointment> appointments;

        if(role.equals("Doctor")) {
            if(!doctorRepo.findByEmail(email).isPresent())
                throw new ObjectNotFound("Doctor not found");
            appointments = appointmentRepo.findByDoctorEmailAndDate(email, date, pageable);
        } else {
           if(!patientRepo.findByEmail(email).isPresent())
               throw new ObjectNotFound("Patient not found");
            appointments = appointmentRepo.findByPatientEmailAndDate(email, date, pageable);
        }

        //log.info("Am ales ziua: " + date + " si s-au gasit " + appointments.getContent().size() + " programari");

        List<AppointmentResponseDto> result = appointments.getContent()
                .stream()
                .map(a -> {
                    AppointmentResponseDto aDto = modelMapper.map(a, AppointmentResponseDto.class);
                    aDto.setDate(a.getTime());
                    aDto.setId(a.getAppointment_id());
                    aDto.setDoctorEmail(a.getDoctor().getEmail());
                    aDto.setPatientEmail(a.getPatient().getEmail());
                    aDto.setNobodyCanceled(a.getPatientIsComing() && a.getDoctorIsAvailable());
                    return aDto;
                }).toList();

        return new PageImpl<>(result, pageable, appointments.getTotalElements());
    }


    @Override
    public boolean checkTimeIsAvailable(List<Appointment> appointments, Date date, String visitType) {
        List<Appointment> sameDayAppointments = appointments.stream()
                .filter(appointment -> isSameDay(appointment.getTime(), date))
                .collect(Collectors.toList());

        // will check how to choose the amount of time based on the visitType
        int amount = 60;
        if(visitType.toLowerCase().equals("consultatie")) {
            amount = 30;
        }

        for (Appointment appointment : sameDayAppointments) {
            Date appointmentStart = subtractMinutes(appointment.getTime(), amount);
            Date appointmentEnd = addMinutes(appointment.getTime(), amount);

            if (date.after(appointmentStart) && date.before(appointmentEnd)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance(); cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance(); cal2.setTime(d2);
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public Date addMinutes(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    @Override
    public Date subtractMinutes(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, -minutes);
        return cal.getTime();
    }
}
