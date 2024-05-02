package com.example.backend.controller;

import com.example.backend.model.exception.InvalidCredentials;
import com.example.backend.security.jwt.JwtUtils;
import com.example.backend.security.payload.request.LoginRequest;
import com.example.backend.security.payload.response.JwtResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authRequest) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwt = jwtUtils.generateToken(userDetails.getUsername(), getRoleFromAuthorities(userDetails.getAuthorities()));

        final String role = jwtUtils.getRoleFromToken(jwt);
        log.info("ROL DIN JWT: " + role);
        return ResponseEntity.ok(new JwtResponse(jwt, role));
    }

    private String getRoleFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            log.info("Authority: " + authority);
            log.info(".getAuthority: " + authority.getAuthority());
            if (authority.getAuthority().startsWith("ROLE_")) {
                log.info("Incepe cu ROLE_!!");
                log.info(authority.getAuthority().substring(5));
                return authority.getAuthority().substring(5).toLowerCase();
            }
        }

        throw new InvalidCredentials("No role associated");
    }
}
