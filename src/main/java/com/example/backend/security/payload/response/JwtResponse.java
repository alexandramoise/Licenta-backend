package com.example.backend.security.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String jwtToken;
    private String role;
    private LocalDateTime availableUntil;
}
