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

    Optional<Voucher> findByMa(String ma);

    boolean existsByMa(String ma);

    // Voucher còn hiệu lực theo thời gian
    @Query("""
            SELECT v FROM Voucher v
            WHERE (:now BETWEEN v.batDauLuc AND v.ketThucLuc)
            """)
    List<Voucher> findActive(@Param("now") LocalDateTime now);

    // Tìm theo trạng thái thời gian
    @Query("""
            SELECT v FROM Voucher v
            WHERE (:now BETWEEN v.batDauLuc AND v.ketThucLuc)
            """)
    Page<Voucher> findAvailable(@Param("now") LocalDateTime now, Pageable pageable);

    // Lấy tất cả voucher, sắp xếp theo ngày tạo mới nhất
    @Query("SELECT v FROM Voucher v ORDER BY v.taoLuc DESC")
    List<Voucher> findAllOrderByTaoLucDesc();
}