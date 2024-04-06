package com.example.backend.service;

import com.example.backend.model.dto.ChangePasswordDto;
import com.example.backend.model.exception.InvalidAccountType;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public boolean changePassword(ChangePasswordDto changePasswordDto, String accountType) throws IllegalAccessException;

}
