package com.example.backend.model.repo;

import com.example.backend.model.entity.table.BloodPressure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodPressureRepo extends JpaRepository<BloodPressure, Long> {
    @Query("SELECT b FROM BloodPressure b JOIN b.patient p WHERE p.email = :patientEmail")
    Page<BloodPressure> findByPatientEmail(@Param("patientEmail") String patientEmail, Pageable pageable);
}
