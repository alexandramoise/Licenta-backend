package com.example.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table(name="BloodPressure")
@AllArgsConstructor
@NoArgsConstructor
public class BloodPressure {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "bloodPressure_id")
    private Long bloodPressure_id;

    @Column
    private Integer systolic;

    @Column
    private Integer diastolic;

    @Column
    private Integer pulse;

    @Column
    private Date date;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @JsonBackReference
    private Patient patient;
}
