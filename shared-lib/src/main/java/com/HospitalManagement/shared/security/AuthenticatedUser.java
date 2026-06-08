package com.HospitalManagement.shared.security;

import com.HospitalManagement.shared.enums.Roles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements UserDetails {
    private final Long userId;
    private final String email;
    private final String name;
    private final Roles role;

    public AuthenticatedUser(Long userId, String email, String name, Roles role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Roles getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
