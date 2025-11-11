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

    // Tìm theo trạng thái thời gian + còn số lượng
    @Query("""
            SELECT v FROM Voucher v
            WHERE (:now BETWEEN v.batDauLuc AND v.ketThucLuc)
              AND (v.gioiHanSuDung IS NULL OR v.gioiHanSuDung > 0 OR v.gioiHanSuDung = -1)
            """)
    Page<Voucher> findAvailable(@Param("now") LocalDateTime now, Pageable pageable);

    // Tăng số lượng đã dùng (dùng khi áp voucher thành công)
    @Modifying
    @Query("UPDATE Voucher v SET v.gioiHanSuDung = v.gioiHanSuDung - 1 " +
            "WHERE v.id = :id AND v.gioiHanSuDung IS NOT NULL AND v.gioiHanSuDung > 0")
    int decreaseLimit(@Param("id") Long id);
}