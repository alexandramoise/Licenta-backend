package com.example.backend.model.repo;

import com.example.backend.model.entity.table.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalConditionRepo extends JpaRepository<MedicalCondition, Long> {
    Optional<MedicalCondition> findByName(String medicalConditionName);
}
