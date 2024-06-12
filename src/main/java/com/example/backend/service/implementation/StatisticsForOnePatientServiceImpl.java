package com.example.backend.service.implementation;

import com.example.backend.model.dto.response.BloodPressureResponseDto;
import com.example.backend.model.dto.response.StatisticsForOnePatientDto;
import com.example.backend.model.entity.table.Appointment;
import com.example.backend.model.entity.table.MedicalCondition;
import com.example.backend.model.repo.AppointmentRepo;
import com.example.backend.model.repo.PatientMedicalConditionRepo;
import com.example.backend.service.BloodPressureService;
import com.example.backend.service.StatisticsForOnePatientService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsForOnePatientServiceImpl implements StatisticsForOnePatientService {
    private final AppointmentRepo appointmentRepo;
    private final BloodPressureService bloodPressureService;
    private final PatientMedicalConditionRepo patientMedicalConditionRepo;

    public StatisticsForOnePatientServiceImpl(AppointmentRepo appointmentRepo, BloodPressureService bloodPressureService, PatientMedicalConditionRepo patientMedicalConditionRepo) {
        this.appointmentRepo = appointmentRepo;
        this.bloodPressureService = bloodPressureService;
        this.patientMedicalConditionRepo = patientMedicalConditionRepo;
    }

    @Override
    public List<Object> getAverageAndExtremeValues(String patientEmail, String fromDate, String toDate) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "date"));
        List<BloodPressureResponseDto> bloodPressures = bloodPressureService.getPatientBPsByTime(patientEmail, fromDate, toDate, pageable).getContent();

        if (bloodPressures.size() == 0) {
            return List.of(0.0, 0.0, 0.0, new BloodPressureResponseDto(), new BloodPressureResponseDto());
        }

        double totalSystolic = 0.0;
        double totalDiastolic = 0.0;
        double totalPulse = 0.0;
        BloodPressureResponseDto maxBp = null;
        BloodPressureResponseDto minBp = null;

        for (BloodPressureResponseDto b : bloodPressures) {
            totalSystolic += b.getSystolic();
            totalDiastolic += b.getDiastolic();
            totalPulse += b.getPulse();

            if (maxBp == null || b.getSystolic() > maxBp.getSystolic() ||
                    (b.getSystolic() == maxBp.getSystolic() && b.getDiastolic() > maxBp.getDiastolic())) {
                maxBp = b;
            }

            if (minBp == null || b.getSystolic() < minBp.getSystolic() ||
                    (b.getSystolic() == minBp.getSystolic() && b.getDiastolic() < minBp.getDiastolic())) {
                minBp = b;
            }
        }

        double averageSystolic =  Math.floor(totalSystolic / bloodPressures.size() * 100) / 100;
        double averageDiastolic = Math.floor(totalDiastolic / bloodPressures.size() * 100) / 100;
        double averagePulse = Math.floor(totalPulse / bloodPressures.size() * 100) / 100;

        if(maxBp == null) {
            maxBp = new BloodPressureResponseDto();
        }

        if(minBp == null) {
            minBp = new BloodPressureResponseDto();
        }

        return List.of(averageSystolic, averageDiastolic, averagePulse, maxBp, minBp);
    }


    @Override
    public Integer getNumberOfVisits(String patientEmail, String fromDate, String toDate) {
        List<Appointment> appointments = appointmentRepo.findByTimeInterval(patientEmail, fromDate, toDate);
        return appointments.size();
    }


    @Override
    public List<Object> medicalConditionsFavoringEachType(String patientEmail, String fromDate, String toDate) {
        List<Object[]> pmc = patientMedicalConditionRepo.findCurrentMedicalConditionsByTime(patientEmail, fromDate, toDate);

        int forHyper = 0;
        int forHypo = 0;

        List<MedicalCondition> patientMedicalConditions = pmc.stream().map(pm -> {
            Boolean increasesBP = (Boolean) pm[0];
            Boolean reducesBP = (Boolean) pm[1];
            Long id = (Long) pm[2];
            String name = (String) pm[3];
            return new MedicalCondition(id, name, increasesBP, reducesBP);
        }).collect(Collectors.toList());

        List<MedicalCondition> currentConditions = new ArrayList<>();
        for (MedicalCondition condition : patientMedicalConditions) {
            currentConditions.add(new MedicalCondition(condition.getName(), condition.getIncreasesBP(), condition.getReducesBP()));
            if (condition.getIncreasesBP() != null && condition.getIncreasesBP()) {
                forHyper++;
            }
            if (condition.getReducesBP() != null && condition.getReducesBP()) {
                forHypo++;
            }
        }

        String favores = "";
        if(forHypo > forHyper) {
            favores = "Hipotensiune";
        } else if(forHyper > forHypo) {
            favores = "Hipertensiune";
        } else {
            favores = "Normala";
        }

        return List.of(forHyper, forHypo, favores, currentConditions);
    }

    @Override
    public StatisticsForOnePatientDto generateStatistics(String patientEmail, String fromDate, String toDate) {
        StatisticsForOnePatientDto result = new StatisticsForOnePatientDto();

        List<Object> averageAndExtreme = getAverageAndExtremeValues(patientEmail, fromDate, toDate);
        result.setAverageSystolic((Double) averageAndExtreme.get(0));
        result.setAverageDiastolic((Double) averageAndExtreme.get(1));
        result.setAveragePulse((Double) averageAndExtreme.get(2));
        result.setMaxBp((BloodPressureResponseDto) averageAndExtreme.get(3));
        result.setMinBp((BloodPressureResponseDto) averageAndExtreme.get(4));

        Integer visits = getNumberOfVisits(patientEmail, fromDate, toDate);
        result.setNumberOfVisits(visits);

        List<Object> numberOfMedicalConditions = medicalConditionsFavoringEachType(patientEmail, fromDate, toDate);
        result.setConditionsFavoringHypertension((Integer) numberOfMedicalConditions.get(0));
        result.setConditionsFavoringHypotension((Integer) numberOfMedicalConditions.get(1));
        result.setFavoringCondition((String) numberOfMedicalConditions.get(2));
        result.setConditions((List<MedicalCondition>) numberOfMedicalConditions.get(3));
        return result;
    }
}
