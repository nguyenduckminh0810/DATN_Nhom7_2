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

    // Tìm kiếm bằng email
    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.vaiTro WHERE t.email = :login AND t.trangThai = true")
    Optional<TaiKhoan> findByEmailAndTrangThaiTrue(@Param("login") String login);

    // Tìm kiếm bằng số điện thoại
    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.vaiTro WHERE t.soDienThoai = :login AND t.trangThai = true")
    Optional<TaiKhoan> findBySoDienThoaiAndTrangThaiTrue(@Param("login") String login);

    default Optional<TaiKhoan> findByEmailOrSoDienThoaiAndTrangThaiTrue(String login) {
        Optional<TaiKhoan> byEmail = findByEmailAndTrangThaiTrue(login);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return findBySoDienThoaiAndTrangThaiTrue(login);
    }
}