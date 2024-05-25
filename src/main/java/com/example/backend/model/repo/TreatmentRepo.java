package com.example.backend.model.repo;

import com.example.backend.model.entity.table.BloodPressure;
import com.example.backend.model.entity.table.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepo extends JpaRepository<Treatment, Long> {
    @Query("SELECT t FROM Treatment t " +
            "JOIN t.patient p JOIN t.medicalCondition m " +
            "WHERE p.email = :patientEmail AND m.name = :medicalCondition")
    Page<Treatment> findByPatientEmail(@Param("patientEmail") String patientEmail,
                                       @Param("medicalCondition") String medicalCondition,
                                       Pageable pageable);

    @Query(value = "SELECT * FROM treatment t WHERE t.starting_date BETWEEN CAST(:fromDate AS DATE) AND CAST(CONCAT(:toDate, ' 23:59:59') AS TIMESTAMP) " +
            "AND t.patient_id = (SELECT id FROM patient WHERE email = :patientEmail) " +
            "AND t.medical_cond_id = (SELECT id FROM medical_condition WHERE name = :medicalCondition)", nativeQuery = true)
    List<Treatment> findByDate(@Param("patientEmail") String patientEmail,
                               @Param("medicalCondition") String medicalCondition,
                               @Param("fromDate") String fromDate,
                               @Param("toDate") String toDate);
}
