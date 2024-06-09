package com.example.backend.service;

import com.example.backend.model.dto.response.DoctorResponseDto;
import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.update.DoctorUpdateDto;
import org.springframework.stereotype.Service;

@Service
public interface DoctorService {
    DoctorResponseDto createAccount(String email);

    Boolean getFirstLoginEver(String email);
    DoctorResponseDto updateAccount(DoctorUpdateDto doctorUpdateDto, String email);

    boolean changePassword(ChangePasswordDto changePasswordDto);
    void requestPasswordChange(String email);

    void acceptTerms(String email);
    DoctorResponseDto getDoctorByEmail(String email);

}
