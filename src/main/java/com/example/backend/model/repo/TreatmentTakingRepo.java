package com.example.backend.model.repo;

import com.example.backend.model.entity.table.TreatmentTaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentTakingRepo extends JpaRepository<TreatmentTaking, Long> {
}
