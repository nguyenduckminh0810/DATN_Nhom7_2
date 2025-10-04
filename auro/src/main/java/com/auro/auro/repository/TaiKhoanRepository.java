package com.auro.auro.repository;

import com.auro.auro.model.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {
    Optional<TaiKhoan> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<TaiKhoan> findBySoDienThoai(String soDienThoai);

    boolean existsBySoDienThoai(String soDienThoai);

    // Đăng nhập chỉ nhận tài khoản đang hoạt động
    Optional<TaiKhoan> findByEmailAndTrangThaiTrue(String email);
    
    // Tìm kiếm bằng email hoặc số điện thoại (chỉ tài khoản hoạt động)
    @Query("SELECT t FROM TaiKhoan t WHERE (t.email = :login OR t.soDienThoai = :login) AND t.trangThai = true")
    Optional<TaiKhoan> findByEmailOrSoDienThoaiAndTrangThaiTrue(@Param("login") String login);
}