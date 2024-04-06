package com.example.backend.model.repo;

import com.example.backend.model.entity.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentRepo extends JpaRepository<Treatment, Long> {
    @Query("SELECT t FROM Treatment t " +
            "JOIN t.patient p JOIN t.medicalCondition m " +
            "WHERE p.email = :patientEmail AND m.name = :medicalCondition")
    Page<Treatment> findByPatientEmail(@Param("patientEmail") String patientEmail,
                                       @Param("medicalCondition") String medicalCondition,
                                       Pageable pageable);
}
