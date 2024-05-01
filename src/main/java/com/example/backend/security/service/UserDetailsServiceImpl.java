package com.example.backend.security.service;

import com.example.backend.model.entity.table.Doctor;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public UserDetailsServiceImpl(DoctorRepo doctorRepo, PatientRepo patientRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws ObjectNotFound {
        // checking if the email corresponds to a doctor account
        Optional<Doctor> doctorOpt = doctorRepo.findByEmail(email);
        if(doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            return new User(doctor.getEmail(), doctor.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        }

        // checking if the email corresponds to a patient account
        Optional<Patient> patientOpt = patientRepo.findByEmail(email);
        if(patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            return new User(patient.getEmail(), patient.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")));
        }

        // not an account
        throw new ObjectNotFound("No account for this email");
    }
}
