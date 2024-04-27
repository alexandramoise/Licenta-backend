package com.example.backend.model.repo;

import com.example.backend.model.entity.table.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepo extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByName(String medicineName);
}
