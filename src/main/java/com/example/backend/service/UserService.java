package com.example.backend.service;

import com.example.backend.model.dto.ChangePasswordDto;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public void createAccount(String email, String accountType) throws IllegalAccessException;
    public boolean changePassword(ChangePasswordDto changePasswordDto, String accountType) throws IllegalAccessException;
    public <T> T updateAccount(Object updateDto, String accountType) throws IllegalAccessException;
}
