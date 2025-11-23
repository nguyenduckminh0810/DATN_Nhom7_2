package com.auro.auro.repository;

import com.auro.auro.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.auro.auro.model.TaiKhoan;

import java.time.LocalDateTime;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByTaiKhoan(TaiKhoan taiKhoan);

    Optional<KhachHang> findByTaiKhoan_Email(String email);

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    // Đếm khách hàng mới đăng ký trong khoảng thời gian (dựa trên taiKhoan.taoLuc hoặc khachHang.taoLuc)
    // Nếu khachHang.taoLuc có giá trị thì dùng nó, nếu không thì dùng taiKhoan.taoLuc
    @Query("SELECT COUNT(k) FROM KhachHang k WHERE " +
           "(k.taoLuc IS NOT NULL AND k.taoLuc >= :startDate AND k.taoLuc <= :endDate) OR " +
           "(k.taoLuc IS NULL AND k.taiKhoan IS NOT NULL AND k.taiKhoan.taoLuc >= :startDate AND k.taiKhoan.taoLuc <= :endDate)")
    long countNewCustomersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
