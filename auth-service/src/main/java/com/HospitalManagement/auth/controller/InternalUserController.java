package com.HospitalManagement.auth.controller;

import com.HospitalManagement.auth.entity.User;
import com.HospitalManagement.auth.repository.UserRepository;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public UserSummaryDto getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        return toSummaryDto(user);
    }

    @GetMapping("/email/{email}")
    public UserSummaryDto getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));
        return toSummaryDto(user);
    }

    private UserSummaryDto toSummaryDto(User user) {
        return UserSummaryDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
