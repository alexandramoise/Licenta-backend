package com.example.backend.model.repo;

import com.example.backend.model.entity.table.Doctor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepo extends UserRepo<Doctor> {
    Optional<Doctor> findByEmail(String email);
}
