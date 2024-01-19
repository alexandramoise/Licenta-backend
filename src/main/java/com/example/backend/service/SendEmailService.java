package com.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public interface SendEmailService {
    public void sendCreateAccountEmail(String email, String accountType) throws IllegalAccessException;
}
