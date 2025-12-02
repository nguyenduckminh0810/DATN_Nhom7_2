package com.auro.auro.repository;

import com.auro.auro.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // Chỉ tìm voucher chưa bị hủy (trangThai != 0)
    @Query("SELECT v FROM Voucher v WHERE v.ma = :ma AND (v.trangThai IS NULL OR v.trangThai <> 0)")
    Optional<Voucher> findByMa(@Param("ma") String ma);

    boolean existsByMa(String ma);

    // Voucher còn hiệu lực theo thời gian cho FE/user: chỉ lấy trangThai = 1 (Active)
    @Query("""
            SELECT v FROM Voucher v
            WHERE v.trangThai = 1
              AND (:now BETWEEN v.batDauLuc AND v.ketThucLuc)
            """)
    List<Voucher> findActive(@Param("now") LocalDateTime now);

    // Tìm theo trạng thái thời gian cho API "co-san": chỉ lấy trangThai = 1 (Active)
    @Query("""
            SELECT v FROM Voucher v
            WHERE v.trangThai = 1
              AND (:now BETWEEN v.batDauLuc AND v.ketThucLuc)
            """)
    Page<Voucher> findAvailable(@Param("now") LocalDateTime now, Pageable pageable);

    // Lấy tất cả voucher còn tồn tại (không bao gồm voucher đã hủy), sắp xếp theo ngày tạo mới nhất
    @Query("SELECT v FROM Voucher v WHERE v.trangThai <> 0 ORDER BY v.taoLuc DESC")
    List<Voucher> findAllOrderByTaoLucDesc();
}