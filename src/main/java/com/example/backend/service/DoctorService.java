package com.example.backend.service;

import com.example.backend.model.dto.*;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Patient;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
@Service
public interface DoctorService {
    DoctorResponseDto createAccount(String email);
    DoctorResponseDto updateAccount(DoctorUpdateDto doctorUpdateDto);

    public boolean changePassword(ChangePasswordDto changePasswordDto);
    public DoctorResponseDto updateDoctor(DoctorUpdateDto doctorUpdateDto);

    public DoctorResponseDto getDoctorByEmail(String email);

    public Page getDoctorsPatients(Pageable pageable);
}
