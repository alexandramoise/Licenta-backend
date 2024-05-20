package com.example.backend.model.entity.table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name="MedicalCondition")
@AllArgsConstructor
@NoArgsConstructor
public class MedicalCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Boolean increasesBP;

    @Column
    private Boolean reducesBP;

    @ManyToMany
    @JoinTable(
            name = "medicalCond_medicine",
            joinColumns = @JoinColumn(name = "medicalCond_id"),
            inverseJoinColumns = @JoinColumn(name = "medicine_id"))
    private List<Medicine> medicines;

    @OneToMany(mappedBy = "medicalCondition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Treatment> treatments;

    @OneToMany(mappedBy = "medicalCondition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PatientMedicalCondition> patient_medicalConditions;

//    @ManyToMany(mappedBy = "medicalConditions")
//    private List<Patient> patients;
}
