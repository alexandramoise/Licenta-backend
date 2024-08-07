package com.example.backend.service;

import com.example.backend.model.dto.MedicalConditionDto;
import com.example.backend.model.dto.response.PatientResponseDto;
import com.example.backend.model.dto.update.ChangePasswordDto;
import com.example.backend.model.dto.update.PatientUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface PatientService {
    PatientResponseDto createAccount(String email, String doctorEmail);
    PatientResponseDto updateAccount(PatientUpdateDto patientUpdateDto, String email);
    boolean changePassword(ChangePasswordDto changePasswordDto);
    void toggleNotifications(String email);
    void deactivateAccount(String email);
    Boolean getFirstLoginEver(String email);
    void requestPasswordChange(String email);
    void acceptTerms(String email);
    void acceptSharingData(String email);
    List<PatientResponseDto> getAllPatients(String doctorEmail);

    Page<PatientResponseDto> getFilteredPagedPatients(String doctorEmail,
                                                      String name, String gender, Integer minAge, Integer maxAge, String type, Pageable pageable);
    Page<PatientResponseDto> getAllPagedPatients(String doctorEmail, Pageable pageable);
    PatientResponseDto getPatientById(Long id);

    PatientResponseDto getPatientByEmail(String email);

    Integer calculateAge(Date dateOfBirth);

    List<MedicalConditionDto> getPatientsMedicalConditions(Long id);

}
