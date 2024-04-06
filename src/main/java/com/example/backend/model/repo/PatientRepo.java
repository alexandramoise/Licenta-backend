package com.example.backend.model.repo;

import com.example.backend.model.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepo extends UserRepo<Patient> {
    Optional<Patient> findByEmail(String email);

    @Query("SELECT p FROM Patient p JOIN p.doctor d WHERE d.email = :doctorEmail")
    Page<Patient> findByDoctorEmail(@Param("doctorEmail") String doctorEmail, Pageable pageable);
}
