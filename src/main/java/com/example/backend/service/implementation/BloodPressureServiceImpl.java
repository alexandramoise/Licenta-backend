package com.example.backend.service.implementation;

import com.example.backend.model.dto.BloodPressureRequestDto;
import com.example.backend.model.dto.BloodPressureResponseDto;
import com.example.backend.model.entity.BloodPressure;
import com.example.backend.model.entity.BloodPressureType;
import com.example.backend.model.entity.Patient;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.exception.CantBeEdited;
import com.example.backend.model.exception.InvalidValues;
import com.example.backend.model.repo.BloodPressureRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.BloodPressureService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        if(patient.getBloodPressures() == null) {
            patient.setBloodPressures(new ArrayList<>());
        }
        patient.getBloodPressures().add(savedBP);
        bloodPressureRepo.save(savedBP);
        return bloodPressureResponseDto;
    }

    @Override
    public List<BloodPressureResponseDto> getPatientBloodPressures(String patientEmail) throws ObjectNotFound {
        Patient patient = patientRepo.findByEmail(patientEmail).orElseThrow(() -> new ObjectNotFound("No patient account for this email address"));
        List <BloodPressure> bloodPressures = patient.getBloodPressures();
        // sorted BP list by date descending in order to access the most recent value and set it as editable.
        bloodPressures.sort(Comparator.comparing(BloodPressure::getDate).reversed());
        List <BloodPressureResponseDto> result =
                bloodPressures.stream().map(bp -> {
                    BloodPressureResponseDto bloodPressureResponseDto = modelMapper.map(bp, BloodPressureResponseDto.class);
                    bloodPressureResponseDto.setPatientEmailAddress(patientEmail);
                    setBloodPressureType(bloodPressureResponseDto);
                    bloodPressureResponseDto.setIsEditable(bloodPressures.indexOf(bp) == 0);
                    return bloodPressureResponseDto;
                }).toList();
        return result;
    }

    @Override
    public BloodPressureResponseDto updateBloodPressureById(Long id, BloodPressureRequestDto bloodPressureRequestDto) {
        BloodPressure bp = bloodPressureRepo.findById(id).orElseThrow(() -> new ObjectNotFound("BP Not found"));
        String patientEmail = bp.getPatient().getEmail();
        List<BloodPressure> bloodPressures = bp.getPatient().getBloodPressures();
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
        Integer diastolic = bloodPressureResponseDto.getDiastolic();
        Integer systolic = bloodPressureResponseDto.getSystolic();
        Range<Integer> normalSystolic = Range.between(120,129);
        Range<Integer> normalDiastolic = Range.between(80,84);
        Range<Integer> preHyperSystolic = Range.between(130,139);
        Range<Integer> preHyperDiastolic = Range.between(85,89);
        Range<Integer> hypertenstionStage1Systolic = Range.between(140,159);
        Range<Integer> hypertenstionStage1Diastolic = Range.between(90,99);
        Range<Integer> hypertenstionStage2Systolic = Range.between(160,179);
        Range<Integer> hypertenstionStage2Diastolic = Range.between(100,109);
        if(systolic < 90 && diastolic < 60) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Hypotension);
        } else if(systolic < 120 && diastolic < 80) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Optimal);
        } else if(normalSystolic.contains(systolic) && normalDiastolic.contains(diastolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Normal);
        } else if(preHyperSystolic.contains(systolic) && preHyperDiastolic.contains(diastolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Prehypertension);
        } else if(hypertenstionStage1Systolic.contains(systolic) && hypertenstionStage1Diastolic.contains(diastolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Hypertension_stage1);
        } else if(hypertenstionStage2Systolic.contains(systolic) && hypertenstionStage2Diastolic.contains(diastolic)) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Hypertension_stage2);
        } else if(systolic >= 180 && diastolic >= 110) {
            bloodPressureResponseDto.setBloodPressureType(BloodPressureType.Hypertension_stage3);
        } else {
            throw new InvalidValues("Invalid blood pressure values");
        }
    }


}
