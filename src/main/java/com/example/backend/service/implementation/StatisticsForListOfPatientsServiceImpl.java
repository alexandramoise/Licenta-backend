package com.example.backend.service.implementation;

import com.example.backend.model.dto.response.BloodPressureResponseDto;
import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.response.StatisticsForListOfPatientsDto;
import com.example.backend.model.entity.table.Appointment;
import com.example.backend.model.repo.AppointmentRepo;
import com.example.backend.service.BloodPressureService;
import com.example.backend.service.StatisticsForListOfPatientsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class StatisticsForListOfPatientsServiceImpl implements StatisticsForListOfPatientsService {
    private final AppointmentRepo appointmentRepo;
    private final BloodPressureService bloodPressureService;

    public StatisticsForListOfPatientsServiceImpl(AppointmentRepo appointmentRepo, BloodPressureService bloodPressureService) {
        this.appointmentRepo = appointmentRepo;
        this.bloodPressureService = bloodPressureService;
    }

    @Override
    public List<Double> getPercentageForEachType(List<PatientResponseDto> patients, String fromDate, String toDate) {
        int hyperTensive = 0;
        int hypoTensive = 0;
        int normal = 0;
        int numberOfPatients = patients.size();

        if (numberOfPatients == 0) {
            return List.of(0.0, 0.0, 0.0);
        }

        for (PatientResponseDto p : patients) {
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"));
            List<BloodPressureResponseDto> bloodPressures = bloodPressureService.getPatientBPsByTime(p.getEmail(), fromDate, toDate, pageable).getContent();
            if (!bloodPressures.isEmpty()) {
                BloodPressureResponseDto latestBP = bloodPressures.get(0);
                if ("hypertension".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                    hyperTensive++;
                } else if("hypotension".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                    hypoTensive++;
                } else if("normal".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                    normal++;
                }
            } else {
                normal++; // patients with no trackings are considered Normal by default
            }
        }

        double hyperTensivePercentage = (double) hyperTensive / (double) numberOfPatients * 100;
        double hypoTensivePercentage = (double) hypoTensive / (double) numberOfPatients * 100;
        double normalPercentage = (double) normal / (double) numberOfPatients * 100;

        return List.of(hyperTensivePercentage, hypoTensivePercentage, normalPercentage);
    }

    @Override
    public List<Integer> getNumberOfEachGender(List<PatientResponseDto> patients) {
        int women = (int) patients.stream()
                .filter(p -> "feminine".equalsIgnoreCase(p.getGender().toString()))
                .count();

        int men = (int) patients.stream()
                .filter(p -> "masculine".equalsIgnoreCase(p.getGender().toString()))
                .count();

        int other = patients.size() - women - men;

        return List.of(women, men, other);
    }

    @Override
    public List<Integer> getNumberOfWomenWithEachType(List<PatientResponseDto> patients, String fromDate, String toDate) {
        int womenWithHypertension = 0;
        int womenWithHypotension = 0;
        int womenWithNormal = 0;

        List<PatientResponseDto> women = patients.stream()
                .filter(p -> p.getGender().toString().equalsIgnoreCase("feminine"))
                .collect(Collectors.toList());

        if(women.size() == 0) {
            return List.of(0, 0, 0);
        } else {
            for (PatientResponseDto p : women) {
                Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"));
                List<BloodPressureResponseDto> bloodPressures = bloodPressureService.getPatientBPsByTime(p.getEmail(), fromDate, toDate, pageable).getContent();
                if (!bloodPressures.isEmpty()) {
                    BloodPressureResponseDto latestBP = bloodPressures.get(0);
                    if ("hypertension".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                        womenWithHypertension++;
                    } else if ("hypotension".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                        womenWithHypotension++;
                    }
                }
            }
            womenWithNormal = women.size() - womenWithHypertension - womenWithHypotension;
        }

        return List.of(womenWithHypertension, womenWithNormal, womenWithHypotension);
    }

    @Override
    public List<Integer> getNumberOfMenWithEachType(List<PatientResponseDto> patients, String fromDate, String toDate) {
        int menWithHypertension = 0;
        int menWithHypotension = 0;
        int menWithNormal = 0;

        List<PatientResponseDto> men = patients.stream()
                .filter(p -> p.getGender().toString().equalsIgnoreCase("masculine"))
                .collect(Collectors.toList());

        if(men.size() == 0) {
            return List.of(0, 0, 0);
        } else {
            for (PatientResponseDto p : men) {
                Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"));
                List<BloodPressureResponseDto> bloodPressures = bloodPressureService.getPatientBPsByTime(p.getEmail(), fromDate, toDate, pageable).getContent();
                if (!bloodPressures.isEmpty()) {
                    BloodPressureResponseDto latestBP = bloodPressures.get(0);
                    if ("hypertension".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                        menWithHypertension++;
                    } else if ("hypotension".equalsIgnoreCase(latestBP.getBloodPressureType().toString())) {
                        menWithHypotension++;
                    }
                }
            }
            menWithNormal = men.size() - menWithHypertension - menWithHypotension;
        }

        return List.of(menWithHypertension, menWithNormal, menWithHypotension);
    }

    @Override
    public List<Object> getPatientsWithExtremeBloodPressures(List<PatientResponseDto> patients, String fromDate, String toDate) {
        if (patients.isEmpty()) {
            return List.of("-", "-", new BloodPressureResponseDto(), new BloodPressureResponseDto());
        }

        BloodPressureResponseDto maxBP = null;
        BloodPressureResponseDto minBP = null;
        String patientWithMaxBP = "";
        String patientWithMinBP = "";

        for (PatientResponseDto p : patients) {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            List<BloodPressureResponseDto> bloodPressures = bloodPressureService.getPatientBPsByTime(p.getEmail(), fromDate, toDate, pageable).getContent();

            if(bloodPressures.size() > 0) {
                for (BloodPressureResponseDto bp : bloodPressures) {
                    if (maxBP == null || (bp.getSystolic() > maxBP.getSystolic() || (bp.getSystolic() == maxBP.getSystolic() && bp.getDiastolic() > maxBP.getDiastolic()))) {
                        maxBP = bp;
                        patientWithMaxBP = p.getFullName();
                    }
                    if (minBP == null || (bp.getSystolic() < minBP.getSystolic() || (bp.getSystolic() == minBP.getSystolic() && bp.getDiastolic() < minBP.getDiastolic()))) {
                        minBP = bp;
                        patientWithMinBP = p.getFullName();
                    }
                }
            }
        }

        if(maxBP == null) {
            maxBP = new BloodPressureResponseDto();
        }

        if(minBP == null) {
            minBP = new BloodPressureResponseDto();
        }

        return List.of(patientWithMaxBP, patientWithMinBP, maxBP, minBP);
    }

    @Override
    public List<Object> getPatientWithMostVisits(List<PatientResponseDto> patients, String fromDate, String toDate) {
        int maxVisits = 0;
        String patientWithMaxVisits = "";

        if(patients.size() == 0) {
            return List.of("-", 0);
        } else {
            for(PatientResponseDto p : patients) {
                List<Appointment> appointments = appointmentRepo.findByTimeInterval(p.getEmail(), fromDate, toDate);
                if(appointments.size() > 0) {
                    if(appointments.size() > maxVisits) {
                        maxVisits = appointments.size();
                        patientWithMaxVisits = p.getFullName();
                    }
                }
            }
            return List.of(patientWithMaxVisits, maxVisits);
        }
    }

    @Override
    public StatisticsForListOfPatientsDto generateStatistics(List<PatientResponseDto> patients, String fromDate, String toDate) {
        StatisticsForListOfPatientsDto result = new StatisticsForListOfPatientsDto();

        result.setTotalNumberOfPatients(patients.size());

        List<Double> typePercentages = getPercentageForEachType(patients, fromDate, toDate);
        result.setHypertensivePercentage(typePercentages.get(0));
        result.setNormalPercentage(typePercentages.get(2));
        result.setHypotensivePercentage(typePercentages.get(1));

        List<Integer> genderDistribution = getNumberOfEachGender(patients);
        result.setMen(genderDistribution.get(1));
        result.setWomen(genderDistribution.get(0));
        result.setOther(genderDistribution.get(2));

        List<Integer> menDistribution = getNumberOfMenWithEachType(patients, fromDate, toDate);
        result.setMenWithHypertension(menDistribution.get(0));
        result.setMenWithHypotension(menDistribution.get(2));
        result.setMenWithNormal(menDistribution.get(1));

        List<Integer> womenDistribution = getNumberOfWomenWithEachType(patients, fromDate, toDate);
        result.setWomenWithHypertension(womenDistribution.get(0));
        result.setWomenWithHypotension(womenDistribution.get(2));
        result.setWomenWithNormal(womenDistribution.get(1));

        List<Object> extremePatientsAndBps = getPatientsWithExtremeBloodPressures(patients, fromDate, toDate);
        result.setPatientWithMaxBloodPressure((String) extremePatientsAndBps.get(0));
        result.setPatientWithMinBloodPressure((String) extremePatientsAndBps.get(1));
        result.setMaxBp((BloodPressureResponseDto) extremePatientsAndBps.get(2));
        result.setMinBp((BloodPressureResponseDto) extremePatientsAndBps.get(3));

        List<Object> mostVisits = getPatientWithMostVisits(patients, fromDate, toDate);
        result.setPatientWithMostVisits((String) mostVisits.get(0));
        result.setMaxVisits((Integer) mostVisits.get(1));
        return result;
    }
}
