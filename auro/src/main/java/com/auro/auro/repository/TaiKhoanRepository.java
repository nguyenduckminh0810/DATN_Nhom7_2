package com.auro.auro.repository;

import com.auro.auro.model.TaiKhoan;
import com.auro.auro.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // Find by email without checking status
    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.vaiTro WHERE t.email = :login")
    Optional<TaiKhoan> findByEmailIgnoreStatus(@Param("login") String login);

    // Find by phone without checking status
    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.vaiTro WHERE t.soDienThoai = :login")
    Optional<TaiKhoan> findBySoDienThoaiIgnoreStatus(@Param("login") String login);

    // Find by email or phone without checking status
    default Optional<TaiKhoan> findByEmailOrSoDienThoaiIgnoreStatus(String login) {
        Optional<TaiKhoan> byEmail = findByEmailIgnoreStatus(login);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return findBySoDienThoaiIgnoreStatus(login);
    }

    @Query("SELECT COUNT(tk) FROM TaiKhoan tk WHERE tk.vaiTro.ma = 'CUS' AND tk.taoLuc BETWEEN :from AND :to")
    long countNewCustomersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(tk) FROM TaiKhoan tk WHERE tk.vaiTro.ma = 'CUS'")
    long countAllCustomers();

    long countByVaiTro(VaiTro vaiTro);
}