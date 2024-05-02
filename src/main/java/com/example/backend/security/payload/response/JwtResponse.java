package com.example.backend.security.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String jwtToken;
    private String role;
}
