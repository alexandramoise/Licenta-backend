package com.example.backend.model.repo;

import com.example.backend.model.entity.Appointment;
import com.example.backend.model.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a JOIN a.doctor d WHERE d.email = :doctorEmail")
    Page<Appointment> findByDoctorEmail(@Param("doctorEmail") String doctorEmail, Pageable pageable);

    @Query("SELECT a FROM Appointment a JOIN a.patient p WHERE p.email = :patientEmail")
    Page<Appointment> findByPatientEmail(@Param("patientEmail") String patientEmail, Pageable pageable);
}
