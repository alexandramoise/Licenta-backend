package com.example.backend.service;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.dto.DoctorResponseDto;
import com.example.backend.model.dto.DoctorUpdateDto;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Patient;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
@Service
public interface DoctorService {
    //public DoctorResponseDto createDoctorAccount(String email);

    public boolean changePassword(ChangePasswordDto changePasswordDto);
    public DoctorResponseDto updateDoctor(DoctorUpdateDto doctorUpdateDto);

    public DoctorResponseDto getDoctorByEmail(String email);

    public Page getDoctorsPatients(Pageable pageable);
}
