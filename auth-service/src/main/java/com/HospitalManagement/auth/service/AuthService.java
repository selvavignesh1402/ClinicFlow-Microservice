package com.HospitalManagement.auth.service;

import com.HospitalManagement.auth.dto.AuthenticationRequest;
import com.HospitalManagement.auth.dto.RegisterRequest;
import com.HospitalManagement.auth.dto.AuthenticationResponse;
import com.HospitalManagement.auth.entity.User;
import com.HospitalManagement.shared.enums.Roles;
import com.HospitalManagement.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        logger.info("Attempting to register new user with email: {}", request.getEmail());
        
        if (repository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: User already exists with email: {}", request.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User already exists with email: "
            );
        }

        Roles role = Roles.PATIENT;
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .status("ACTIVE")
                .build();
        repository.save(user);
        
        logger.info("User registered successfully - Name: {}, Email: {}, Phone: {}, Role: {}", 
            request.getName(), request.getEmail(), request.getPhone(), role);

        return AuthenticationResponse.builder()
                .message("User registered successfully")
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        logger.info("Attempting authentication for user with email: {}", request.getEmail());
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed: Invalid credentials for email: {}", request.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: User not found for email: {}", request.getEmail());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        var jwtToken = jwtService.generateToken(user);
        
        logger.info("User authenticated successfully - Email: {}, Role: {}", request.getEmail(), user.getRole());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .message("User authenticated successfully")
                .build();
    }

    public User getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        ));
    }
}
