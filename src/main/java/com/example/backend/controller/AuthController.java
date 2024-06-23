package com.example.backend.controller;

import com.example.backend.model.entity.table.Doctor;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.InvalidCredentials;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.security.jwt.JwtUtils;
import com.example.backend.security.payload.request.LoginRequest;
import com.example.backend.security.payload.response.JwtResponse;
import com.example.backend.service.DoctorService;
import com.example.backend.service.PatientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collection;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private final UserDetailsService userDetailsService;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDetailsService userDetailsService, DoctorRepo doctorRepo, PatientRepo patientRepo) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authRequest) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwt = jwtUtils.generateToken(userDetails.getUsername(), getRoleFromAuthorities(userDetails.getAuthorities()));

        final String role = jwtUtils.getRoleFromToken(jwt);

        if(role.equalsIgnoreCase("doctor")) {
            Doctor doctor = doctorRepo.findByEmail(authRequest.getEmail()).get();
            if(! doctor.getIsActive()) {
                return new ResponseEntity("Inactive account", HttpStatus.BAD_REQUEST);
            }
        } else {
            Patient patient = patientRepo.findByEmail(authRequest.getEmail()).get();
            if(! patient.getIsActive()) {
                return new ResponseEntity("Inactive account", HttpStatus.BAD_REQUEST);
            }
        }
        final LocalDateTime date = LocalDateTime.now();
        final LocalDateTime availableUntil = date.plusDays(1);
        return new ResponseEntity<>(new JwtResponse(jwt, role, availableUntil), HttpStatus.OK);
    }

    private String getRoleFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().startsWith("ROLE_")) {
                return authority.getAuthority().substring(5).toLowerCase();
            }
        }

        throw new InvalidCredentials("No role associated");
    }
}
