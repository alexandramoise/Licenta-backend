package com.example.backend.model.dto.update;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String email;
    private String oldPassword;
    private String newPassword;
}
