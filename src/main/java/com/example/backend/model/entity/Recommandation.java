package com.example.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;

@Data
@Entity
@Table(name="Recommandation")
@AllArgsConstructor
@NoArgsConstructor
public class Recommandation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String text;

    @Column
    private BloodPressureType recommandationType;
}
