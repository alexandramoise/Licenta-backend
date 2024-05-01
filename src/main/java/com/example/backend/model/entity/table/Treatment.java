package com.example.backend.model.entity.table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="Treatment")
@AllArgsConstructor
@NoArgsConstructor
public class Treatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Date startingDate = new Date();

    @Column
    private Date endingDate = null;

    @Column
    private Integer doses;

    @Column
    private String comment;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @JsonBackReference
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    @JsonBackReference
    private Medicine medicine;

    @ManyToOne
    @JoinColumn(name = "medicalCond_id", referencedColumnName = "id")
    @JsonBackReference
    private MedicalCondition medicalCondition;

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<TreatmentTaking> treatmentAdministrations;
}
