package com.example.backend.model.repo;

import com.example.backend.model.entity.Patient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepo extends UserRepo<Patient> {
    Optional<Patient> findByEmail(String email);
}
