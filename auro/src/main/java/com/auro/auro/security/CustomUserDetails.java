package com.auro.auro.security;

import com.auro.auro.model.TaiKhoan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class CustomUserDetails implements UserDetails {
    private final TaiKhoan tk;

    public CustomUserDetails(TaiKhoan tk) {
        this.tk = tk;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleCode = tk.getVaiTro() != null ? tk.getVaiTro().getMa() : "GUEST";
        return Set.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getPassword() {
        return tk.getMatKhauHash();
    }

    @Override
    public String getUsername() {
        return tk.getEmail(); // Luôn trả về email cho Spring Security
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
        return Boolean.TRUE.equals(tk.getTrangThai());
    }

    public TaiKhoan getTaiKhoan() {
        return tk;
    }
    
    // Helper methods
    public boolean isGuest() {
        return tk.getVaiTro() == null || "GUEST".equals(tk.getVaiTro().getMa());
    }
    
    public boolean isCustomer() {
        return "CUSTOMER".equals(tk.getVaiTro().getMa());
    }
    
    public boolean isStaff() {
        return "STAFF".equals(tk.getVaiTro().getMa());
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(tk.getVaiTro().getMa());
    }
}