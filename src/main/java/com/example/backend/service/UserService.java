package com.example.backend.service;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.exception.InvalidAccountType;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public <T> T createAccount(String email, String accountType, Long doctorId);
    public boolean changePassword(ChangePasswordDto changePasswordDto, String accountType) throws IllegalAccessException;
    public <T> T updateAccount(Object updateDto, String accountType) throws InvalidAccountType;

    public <T> T getAccount(Object updateDto, String accountType);
}
