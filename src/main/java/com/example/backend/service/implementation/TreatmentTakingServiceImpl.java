package com.example.backend.service.implementation;

import com.example.backend.model.dto.request.TreatmentTakingRequestDto;
import com.example.backend.model.dto.response.TreatmentTakingResponseDto;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.entity.table.Treatment;
import com.example.backend.model.entity.table.TreatmentTaking;
import com.example.backend.model.exception.InvalidValues;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.model.repo.TreatmentRepo;
import com.example.backend.model.repo.TreatmentTakingRepo;
import com.example.backend.service.SendEmailService;
import com.example.backend.service.TreatmentTakingService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@Log4j2
public class TreatmentTakingServiceImpl implements TreatmentTakingService {
    private final TreatmentTakingRepo treatmentTakingRepo;
    private final TreatmentRepo treatmentRepo;
    private final PatientRepo patientRepo;
    private final ModelMapper modelMapper;

    private final SendEmailService sendEmailService;

    public TreatmentTakingServiceImpl(TreatmentTakingRepo treatmentTakingRepo, TreatmentRepo treatmentRepo, PatientRepo patientRepo, ModelMapper modelMapper, SendEmailService sendEmailService) {
        this.treatmentTakingRepo = treatmentTakingRepo;
        this.treatmentRepo = treatmentRepo;
        this.patientRepo = patientRepo;
        this.modelMapper = modelMapper;
        this.sendEmailService = sendEmailService;
    }

    @Override
    public TreatmentTakingResponseDto addTreatmentTaking(TreatmentTakingRequestDto treatmentTakingRequestDto) {
        Treatment treatment = treatmentRepo.findById(treatmentTakingRequestDto.getTreatmentId()).orElseThrow(() -> new ObjectNotFound("Treatment not found"));
        Patient patient = patientRepo.findByEmail(treatmentTakingRequestDto.getPatientEmail()).orElseThrow(() -> new ObjectNotFound("Patient not found"));

        Date today = new Date();
        if(treatmentTakingRequestDto.getAdministrationDate().after(today)) {
            throw new InvalidValues("Date can not be in the future");
        }

        if(! patient.equals(treatment.getPatient())) {
            throw new InvalidValues("Patient does not have this treatment assigned");
        }

        if(treatmentTakingRequestDto.getAdministrationDate().before(treatment.getStartingDate())) {
            throw new InvalidValues("Treatment not started yet");
        }

        TreatmentTaking tt = new TreatmentTaking();
        tt.setTreatment(treatment);
        tt.setPatient(patient);
        tt.setAdministrationDate(treatmentTakingRequestDto.getAdministrationDate());
        treatmentTakingRepo.save(tt);

        TreatmentTakingResponseDto result = modelMapper.map(tt, TreatmentTakingResponseDto.class);
        result.setTreatmentId(tt.getTreatment().getId());
        result.setPatientEmail(tt.getPatient().getEmail());
        return result;
    }

    @Override
    public List<TreatmentTakingResponseDto> getTreatmentTakings(Long treatmentId, String patientEmail, String date) {
        if (!treatmentRepo.findById(treatmentId).isPresent()) {
            throw new ObjectNotFound("Treatment not found");
        }

        if (!patientRepo.findByEmail(patientEmail).isPresent()) {
            throw new ObjectNotFound("Patient not found");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate dateInput = LocalDate.parse(date, formatter);

        if (dateInput.isAfter(today)) {
            throw new InvalidValues("Date cannot be in the future");
        }

        List<TreatmentTaking> treatmentTakings = treatmentTakingRepo.getTreatmentTakingsByDate(treatmentId, patientEmail, date);

        treatmentTakings.sort(Comparator.comparing(TreatmentTaking::getAdministrationDate));
        return treatmentTakings.stream()
                .map(tt -> {
                    TreatmentTakingResponseDto responseDto = new TreatmentTakingResponseDto();
                    responseDto.setId(tt.getId());
                    responseDto.setPatientEmail(tt.getPatient().getEmail());
                    responseDto.setTreatmentId(tt.getTreatment().getId());
                    responseDto.setAdministrationDate(tt.getAdministrationDate());
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void checkAndSendAlert(Long treatmentId, String patientEmail) {
        List<TreatmentTaking> treatmentTakings = treatmentTakingRepo.getAllTreatmentTakings(treatmentId, patientEmail);

        if(treatmentTakings.size() != 0) {
            TreatmentTaking latest = treatmentTakingRepo.findLatestTreatmentTaking(treatmentId, patientEmail);
            LocalDateTime lastAdministrationDate = latest.getAdministrationDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            if (lastAdministrationDate.isBefore(LocalDateTime.now().minusDays(1))) {
                sendEmailService.sendTreatmentAdministrationReminder(treatmentId, patientEmail);
            }
        } else {
            sendEmailService.sendTreatmentAdministrationReminder(treatmentId, patientEmail);
        }
    }
    @Override
    @Scheduled(cron = "0 0 12 * * ?", zone = "Europe/Bucharest")
    @Transactional
    public void scheduledChecking() {
        List<String> patientEmails = patientRepo.getAllPatientsEmails();
        for (String email : patientEmails) {
            List<Treatment> treatments = treatmentRepo.getAllCurrentTreatments(email);
            for(Treatment t : treatments) {
                checkAndSendAlert(t.getId(), email);
            }
        }
    }
}
