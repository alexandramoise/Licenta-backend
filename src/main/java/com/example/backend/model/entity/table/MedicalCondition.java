package com.example.backend.model.entity.table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    public MedicalCondition(Long id, String name, Boolean increasesBP, Boolean reducesBP) {
        this.id = id;
        this.name = name;
        this.increasesBP = increasesBP;
        this.reducesBP = reducesBP;
    }

    @ManyToMany
    @JoinTable(
            name = "medicalCond_medicine",
            joinColumns = @JoinColumn(name = "medicalCond_id"),
            inverseJoinColumns = @JoinColumn(name = "medicine_id"))
    @ToString.Exclude
    private List<Medicine> medicines;

    @OneToMany(mappedBy = "medicalCondition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Treatment> treatments;

    @OneToMany(mappedBy = "medicalCondition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PatientMedicalCondition> patient_medicalConditions;

//    @ManyToMany(mappedBy = "medicalConditions")
//    private List<Patient> patients;
}
