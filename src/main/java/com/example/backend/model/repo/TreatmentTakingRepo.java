package com.example.backend.model.repo;

import com.example.backend.model.entity.table.TreatmentTaking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentTakingRepo extends JpaRepository<TreatmentTaking, Long> {
    @Query(value = "SELECT * FROM treatment_taking t WHERE " +
            "t.treatment_id = :treatmentId AND " +
            "t.patient_id = (SELECT id FROM patient WHERE email = :patientEmail) AND " +
            "DATE(t.administration_date) = CAST(:date AS DATE)", nativeQuery = true)
    List<TreatmentTaking> getTreatmentTakingsByDate(@Param("treatmentId") Long treatmentId,
                                                    @Param("patientEmail") String patientEmail,
                                                    @Param("date") String date);

    @Query("SELECT t FROM TreatmentTaking t " +
            "JOIN t.patient p " +
            "JOIN t.treatment tr " +
            "WHERE p.email = :patientEmail AND tr.id = :treatmentId " +
            "ORDER BY t.administrationDate DESC")
    List<TreatmentTaking> getAllTreatmentTakings(@Param("treatmentId") Long treatmentId,
                                                 @Param("patientEmail") String patientEmail);

    @Query("SELECT t FROM TreatmentTaking t " +
            "JOIN t.patient p " +
            "JOIN t.treatment tr " +
            "WHERE p.email = :patientEmail AND tr.id = :treatmentId " +
            "ORDER BY t.administrationDate DESC " +
            "LIMIT 1")
    TreatmentTaking findLatestTreatmentTaking(@Param("treatmentId") Long treatmentId,
                                              @Param("patientEmail") String patientEmail);


}
