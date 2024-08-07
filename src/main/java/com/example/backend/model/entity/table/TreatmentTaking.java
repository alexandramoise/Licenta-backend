package com.example.backend.model.entity.table;

import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.entity.table.Treatment;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@Entity
@Table(name="TreatmentTaking")
@AllArgsConstructor
@NoArgsConstructor
public class TreatmentTaking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @JsonBackReference
    @ToString.Exclude
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "treatment_id", referencedColumnName = "id")
    @JsonBackReference
    @ToString.Exclude
    private Treatment treatment;

    @Column
    private Date administrationDate;
}
