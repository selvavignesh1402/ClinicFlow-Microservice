package com.HospitalManagement.auth.controller;

import com.HospitalManagement.auth.dto.AuthenticationRequest;
import com.HospitalManagement.auth.dto.RegisterRequest;
import com.HospitalManagement.auth.dto.AuthenticationResponse;
import com.HospitalManagement.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
