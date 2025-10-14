package com.auro.auro.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class GuestUserDetails implements UserDetails {
    private final String sessionId;

    public GuestUserDetails(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_GST"));
    }

    @Override
    public String getPassword() {
        return null; // Guest không có password
    }

    @Override
    public String getUsername() {
        return "guest_" + sessionId;
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

    public String getSessionId() {
        return sessionId;
    }
}