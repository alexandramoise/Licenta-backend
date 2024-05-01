package com.example.backend.model.entity.table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name="Medicine")
@AllArgsConstructor
@NoArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToMany(mappedBy = "medicines")
    private List<MedicalCondition> medicalConditions;

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Treatment> treatments;
}
