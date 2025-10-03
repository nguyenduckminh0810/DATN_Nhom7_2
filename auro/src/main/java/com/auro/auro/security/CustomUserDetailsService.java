package com.auro.auro.security;

import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // login có thể là email hoặc số điện thoại
        TaiKhoan taiKhoan = taiKhoanRepository
                .findByEmailOrSoDienThoaiAndTrangThaiTrue(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Không tìm thấy tài khoản với email/số điện thoại: " + login));
        
        return new CustomUserDetails(taiKhoan);
    }
}