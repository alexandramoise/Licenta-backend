package com.example.backend.service.implementation;

import com.example.backend.model.dto.BloodPressureRequestDto;
import com.example.backend.model.dto.BloodPressureResponseDto;
import com.example.backend.model.dto.TreatmentRequestDto;
import com.example.backend.model.dto.TreatmentResponseDto;
import com.example.backend.model.entity.*;
import com.example.backend.model.exception.EmptyList;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.exception.CantBeEdited;
import com.example.backend.model.exception.InvalidValues;
import com.example.backend.model.repo.BloodPressureRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.BloodPressureService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

@Service
@Log4j2
public class BloodPressureServiceImpl implements BloodPressureService {
    private final BloodPressureRepo bloodPressureRepo;
    private final PatientRepo patientRepo;
    private final ModelMapper modelMapper;

    public BloodPressureServiceImpl(BloodPressureRepo bloodPressureRepo, PatientRepo patientRepo, ModelMapper modelMapper) {
        this.bloodPressureRepo = bloodPressureRepo;
        this.patientRepo = patientRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    public BloodPressureResponseDto addBloodPressure(BloodPressureRequestDto bloodPressureRequestDto, String patientEmail) throws ObjectNotFound, InvalidValues {
        BloodPressure savedBP = new BloodPressure();
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("No patient account for this email address"));
        savedBP.setPatient(patient);
        savedBP.setSystolic(bloodPressureRequestDto.getSystolic());
        savedBP.setDiastolic(bloodPressureRequestDto.getDiastolic());
        savedBP.setPulse(bloodPressureRequestDto.getPulse());
        savedBP.setDate(bloodPressureRequestDto.getDate());
        BloodPressureResponseDto bloodPressureResponseDto = modelMapper.map(savedBP, BloodPressureResponseDto.class);
        bloodPressureResponseDto.setPatientEmailAddress(patientEmail);
        bloodPressureResponseDto.setIsEditable(true);
        setBloodPressureType(bloodPressureResponseDto);
//        if(patient.getBloodPressures() == null) {
//            patient.setBloodPressures(new ArrayList<>());
//        }
//        patient.getBloodPressures().add(savedBP);
        bloodPressureRepo.save(savedBP);
        bloodPressureResponseDto.setId(savedBP.getBloodPressure_id());
        return bloodPressureResponseDto;
    }

    @Override
    public List<BloodPressureResponseDto> getPatientBloodPressures(String patientEmail) throws ObjectNotFound {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("No patient account for this email address"));
        List <BloodPressure> bloodPressures = patient.getBloodPressures();

        if(bloodPressures.size() > 0) {
            // sorted BP list by date descending in order to access the most recent value and set it as editable.
            bloodPressures.sort(Comparator.comparing(BloodPressure::getDate).reversed());
            List<BloodPressureResponseDto> result =
                    bloodPressures.stream().map(bp -> {
                        BloodPressureResponseDto bloodPressureResponseDto = modelMapper.map(bp, BloodPressureResponseDto.class);
                        bloodPressureResponseDto.setPatientEmailAddress(patientEmail);
                        setBloodPressureType(bloodPressureResponseDto);
                        bloodPressureResponseDto.setIsEditable(bloodPressures.indexOf(bp) == 0);
                        bloodPressureResponseDto.setId(bp.getBloodPressure_id());
                        return bloodPressureResponseDto;
                    }).toList();
            return result;
        } else return new ArrayList<>();
    }

    @Override
    public Page<BloodPressureResponseDto> getPagedBloodPressures(String patientEmail, Pageable pageable) throws ObjectNotFound {
        if(!patientRepo.findByEmail(patientEmail).isPresent())
            throw new ObjectNotFound("No patient account for this email address");

        List<BloodPressure> bloodPressures = bloodPressureRepo.findByPatientEmail(patientEmail, pageable).getContent();

        List<BloodPressureResponseDto> result =
                bloodPressures
                    .stream()
                    .map(bp -> {
                        BloodPressureResponseDto bloodPressureResponseDto = modelMapper.map(bp, BloodPressureResponseDto.class);
                        bloodPressureResponseDto.setPatientEmailAddress(patientEmail);
                        setBloodPressureType(bloodPressureResponseDto);
                        bloodPressureResponseDto.setIsEditable(bloodPressures.indexOf(bp) == 0);
                        bloodPressureResponseDto.setId(bp.getBloodPressure_id());
                        return bloodPressureResponseDto;
                }).toList();
        return new PageImpl<>(result, pageable, result.size());
    }

    @Override
    public Map<Date, BloodPressureType> getPatientBPTendencyOverTime(String patientEmail) throws ObjectNotFound {
        Patient patient =  patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("No patient account with this email"));
        Map<Date, BloodPressureType> result = new HashMap<>();
        List<BloodPressureResponseDto> bloodPressures = getPatientBloodPressures(patientEmail);
        for(BloodPressureResponseDto bp : bloodPressures) {
            result.put(bp.getDate(), bp.getBloodPressureType());
        }
        return result;
    }

    @Override
    public BloodPressureType getCurrentBPType(String patientEmail) throws ObjectNotFound {
        Patient patient =  patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("No patient account with this email"));

        if(patient.getBloodPressures().size() == 0) {
            throw new EmptyList("No blood pressures introduced by this account");
        }

        BloodPressureResponseDto latest = getPatientBloodPressures(patientEmail).get(0);
        return latest.getBloodPressureType();
    }

    @Override
    public BloodPressureResponseDto updateBloodPressureById(Long id, BloodPressureRequestDto bloodPressureRequestDto) {
        BloodPressure bp = bloodPressureRepo.findById(id).orElseThrow(() -> new ObjectNotFound("BP Not found"));
        String patientEmail = bp.getPatient().getEmail();
        List<BloodPressure> bloodPressures = bp.getPatient().getBloodPressures();

        if(bloodPressures.size() == 0) {
            throw new EmptyList("No blood pressures introduced by this account");
        }

        bloodPressures.sort(Comparator.comparing(BloodPressure::getDate).reversed());

        if(bloodPressures.indexOf(bp) != 0) {
            throw new CantBeEdited("Uneditable!");
        }

        bp.setSystolic(bloodPressureRequestDto.getSystolic());
        bp.setDiastolic(bloodPressureRequestDto.getDiastolic());
        bp.setPulse(bloodPressureRequestDto.getPulse());
        bp.setDate(bloodPressureRequestDto.getDate());
        bloodPressureRepo.save(bp);

        BloodPressureResponseDto result = modelMapper.map(bp, BloodPressureResponseDto.class);
        result.setPatientEmailAddress(patientEmail);
        result.setIsEditable(true);
        result.setId(bp.getBloodPressure_id());
        setBloodPressureType(result);

        return result;
    }

    @Override
    public void deleteBloodPressureById(Long id) {
        if(bloodPressureRepo.existsById(id)) {
            bloodPressureRepo.deleteById(id);
        } else {
            throw new ObjectNotFound("BP Not found");
        }
    }

    public void setBloodPressureType(BloodPressureResponseDto bloodPressureResponseDto) throws InvalidValues {
        Integer patientDiastolic = bloodPressureResponseDto.getDiastolic();
        Integer patientSystolic = bloodPressureResponseDto.getSystolic();

        Patient patient = patientRepo.findByEmail(bloodPressureResponseDto.getPatientEmailAddress())
                .orElseThrow(() -> new ObjectNotFound("No patient with this email address"));

        Integer patientAge = calculateAge(patient.getDateOfBirth());
        BloodPressureForAge bloodPressureForAge = new BloodPressureForAge();
        BloodPressureRange lowRange = bloodPressureForAge.getLowRange(patientAge);
        BloodPressureRange normalRange = bloodPressureForAge.getNormalRange(patientAge);
        BloodPressureRange highRange = bloodPressureForAge.getHighRange(patientAge);

        if (lowRange.getDiastolic().contains(patientDiastolic) && lowRange.getSystolic().contains(patientSystolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Hypotension);
        } else if (normalRange.getDiastolic().contains(patientDiastolic) && normalRange.getSystolic().contains(patientSystolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Normal);
        } else if (highRange.getDiastolic().contains(patientDiastolic) && highRange.getSystolic().contains(patientSystolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Hypertension);
        } else throw new InvalidValues("Invalid values for diastolic and/or systolic");
    }

    public Integer calculateAge(Date dateOfBirth) {
        LocalDate bdayLocalDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentLocalDate = LocalDate.now();
        Period period = Period.between(bdayLocalDate, currentLocalDate);
        return period.getYears();
    }
}
