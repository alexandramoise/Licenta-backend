package com.example.backend.model.entity.table;

import com.example.backend.model.entity.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="Patient")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Patient extends User {

    @Column
    private Date dateOfBirth = new Date();

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.Other;

    @Enumerated(EnumType.STRING)
    private BloodPressureType currentType = BloodPressureType.Normal;

    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @JsonBackReference
    private Doctor doctor;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BloodPressure> bloodPressures;

//    @ManyToMany
//    @JoinTable(
//            name = "patient_medicalCond",
//            joinColumns = @JoinColumn(name = "patient_id"),
//            inverseJoinColumns = @JoinColumn(name = "medicalCond_id"))
//    private List<MedicalCondition> medicalConditions;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Treatment> treatments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<PatientMedicalCondition> patient_medicalconditions;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<TreatmentTaking> treatmentAdministrations;
}
