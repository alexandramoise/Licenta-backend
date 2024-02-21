package com.example.backend.service.implementation;

import com.example.backend.model.dto.PatientResponseDto;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Patient;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.BloodPressureService;
import com.example.backend.service.PatientService;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

@Service
public class PatientServiceImpl implements PatientService  {
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final ModelMapper modelMapper;
    private final BloodPressureService bloodPressureService;

    public PatientServiceImpl(PatientRepo patientRepo, DoctorRepo doctorRepo, ModelMapper modelMapper, BloodPressureService bloodPressureService) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.modelMapper = modelMapper;
        this.bloodPressureService = bloodPressureService;
    }

    @Override
    public List<PatientResponseDto> getAllPatients(String doctorEmail) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor account with this address"));
        List<Patient> patients = doctor.getPatients();
        List<PatientResponseDto> result =
                patients.stream().map((p) -> {
                    PatientResponseDto pDto = modelMapper.map(p, PatientResponseDto.class);
                    pDto.setFullName(p.getLastName().concat(", " + p.getFirstName()));
                    pDto.setDoctorEmailAddress(doctorEmail);
                    pDto.setAge(calculateAge(p.getDateOfBirth()));
                    return pDto;
                }).toList();
        return result;
     }

    @Override
    public PatientResponseDto getPatientById(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ObjectNotFound("No patient with this id"));
        PatientResponseDto pDto = modelMapper.map(patient, PatientResponseDto.class);
        pDto.setFullName(patient.getLastName().concat(" " + patient.getFirstName()));
        pDto.setDoctorEmailAddress(patient.getDoctor().getEmail());
        pDto.setAge(calculateAge(patient.getDateOfBirth()));
        return pDto;
    }

    @Override
    public Integer calculateAge(Date dateOfBirth) {
        LocalDate bdayLocalDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentLocalDate = LocalDate.now();
        Period period = Period.between(bdayLocalDate, currentLocalDate);
        return period.getYears();
    }
}
