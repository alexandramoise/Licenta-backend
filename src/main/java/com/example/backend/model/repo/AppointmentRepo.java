package com.example.backend.model.repo;

import com.example.backend.model.entity.table.Appointment;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a JOIN a.doctor d WHERE d.email = :doctorEmail")
    Page<Appointment> findByDoctorEmail(@Param("doctorEmail") String doctorEmail, Pageable pageable);

    @Query("SELECT a FROM Appointment a JOIN a.patient p WHERE p.email = :patientEmail")
    Page<Appointment> findByPatientEmail(@Param("patientEmail") String patientEmail, Pageable pageable);

    @Query(value = "SELECT * FROM appointment a WHERE DATE(a.time) = CAST(:date AS DATE) " +
            "AND a.doctor_id = (SELECT id FROM doctor WHERE email = :doctorEmail)", nativeQuery = true)
    Page<Appointment> findByDoctorEmailAndDate(@Param("doctorEmail") String doctorEmail,
                                               @Param("date") String date,
                                               Pageable pageable);

    @Query(value = "SELECT * FROM appointment a WHERE DATE(a.time) = CAST(:date AS DATE) " +
            "AND a.patient_id = (SELECT id FROM patient WHERE email = :patientEmail)", nativeQuery = true)
    Page<Appointment> findByPatientEmailAndDate(@Param("patientEmail") String patientEmail,
                                                @Param("date") String date,
                                                Pageable pageable);

    @Query("SELECT a FROM Appointment a JOIN a.patient p WHERE p.email = :patientEmail AND a.time < current date" +
            " ORDER BY a.time DESC" +
            " LIMIT  1")
    Appointment findLatestOfPatientsAppointments(@Param("patientEmail") String patientEmail);

    @Query(value = "SELECT * FROM appointment a WHERE a.time BETWEEN CAST(:fromDate AS DATE) AND CAST(CONCAT(:toDate, ' 23:59:59') AS TIMESTAMP) " +
            "AND a.patient_id = (SELECT id FROM patient WHERE email = :patientEmail)" +
            "ORDER BY a.time DESC", nativeQuery = true)
    List<Appointment> findByTimeInterval(@Param("patientEmail") String patientEmail,
                                         @Param("fromDate") String fromDate,
                                         @Param("toDate") String toDate);
}
