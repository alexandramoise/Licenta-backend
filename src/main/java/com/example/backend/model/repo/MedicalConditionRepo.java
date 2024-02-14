package com.example.backend.model.repo;

import com.example.backend.model.entity.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalConditionRepo extends JpaRepository<MedicalCondition, Long> {
}
