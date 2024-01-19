package com.example.backend.model.repo;

import com.example.backend.model.entity.BloodPressure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodPressureRepo extends JpaRepository<BloodPressure, Long> {
}
