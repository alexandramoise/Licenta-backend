package com.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public interface SendEmailService {
    public <T> T sendCreateAccountEmail(String email, String accountType);
}
