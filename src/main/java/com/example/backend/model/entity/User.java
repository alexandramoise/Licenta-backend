package com.example.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String firstName = "first_name";

    @Column
    private String lastName = "last_name";

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private Boolean isActive = true;

    @Column
    private Boolean firstLoginEver;
}
