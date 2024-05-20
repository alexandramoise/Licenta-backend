package com.example.backend.model.entity.table;

import com.example.backend.model.entity.table.MedicalCondition;
import com.example.backend.model.entity.table.Patient;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table(name="PatientMedicalCondition")
@AllArgsConstructor
@NoArgsConstructor
public class PatientMedicalCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Date startingDate = new Date();

    @Column
    private Date endingDate = null;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @JsonBackReference
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicalCond_id", referencedColumnName = "id")
    @JsonBackReference
    private MedicalCondition medicalCondition;
}
