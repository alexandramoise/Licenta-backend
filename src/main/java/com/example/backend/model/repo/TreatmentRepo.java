package com.example.backend.model.repo;

import com.example.backend.model.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentRepo extends JpaRepository<Treatment, Long> {
}
