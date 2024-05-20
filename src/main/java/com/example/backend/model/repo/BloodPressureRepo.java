package com.example.backend.model.repo;

import com.example.backend.model.entity.table.BloodPressure;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodPressureRepo extends JpaRepository<BloodPressure, Long> {
    @Query("SELECT b FROM BloodPressure b JOIN b.patient p WHERE p.email = :patientEmail")
    Page<BloodPressure> findByPatientEmail(@Param("patientEmail") String patientEmail, Pageable pageable);

    /* used "CONCAT(date, ' 23:59:59') so that the selected interval includes the entire ending day */
    @Query(value = "SELECT * FROM blood_pressure b WHERE b.date BETWEEN CAST(:fromDate AS DATE) AND CAST(CONCAT(:toDate, ' 23:59:59') AS TIMESTAMP) " +
            "AND b.patient_id = (SELECT id FROM patient WHERE email = :patientEmail)", nativeQuery = true)
    List<BloodPressure> findByDate(@Param("patientEmail") String patientEmail,
                                   @Param("fromDate") String fromDate,
                                   @Param("toDate") String toDate);


}
