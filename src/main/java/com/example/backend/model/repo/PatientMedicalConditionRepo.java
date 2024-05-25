package com.example.backend.model.repo;

import com.example.backend.model.entity.table.MedicalCondition;
import com.example.backend.model.entity.table.PatientMedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientMedicalConditionRepo extends JpaRepository<PatientMedicalCondition, Long> {
    @Query(value = "SELECT mc.* FROM medical_condition mc " +
            "JOIN patient_medical_condition pmc ON mc.id = pmc.medical_cond_id " +
            "JOIN patient p ON p.id = pmc.patient_id " +
            "WHERE p.email = :patientEmail " +
            "AND pmc.ending_date IS NULL " +
            "AND pmc.starting_date BETWEEN CAST(:fromDate AS TIMESTAMP) AND CAST(CONCAT(:toDate, ' 23:59:59') AS TIMESTAMP)",
            nativeQuery = true)
    List<Object[]> findCurrentMedicalConditionsByTime(@Param("patientEmail") String patientEmail,
                                                              @Param("fromDate") String fromDate,
                                                              @Param("toDate") String toDate);

}
