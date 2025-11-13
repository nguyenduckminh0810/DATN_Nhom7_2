package com.auro.auro.repository;

import com.auro.auro.model.DonHangChiTiet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DonHangChiTietRepository extends JpaRepository<DonHangChiTiet, Long> {
    List<DonHangChiTiet> findByDonHang_Id(Long idDonHang);

    // Find order items by variant id (bien the)
    java.util.List<DonHangChiTiet> findByBienThe_Id(Long idBienThe);

    // Count order items by variant id
    long countByBienThe_Id(Long idBienThe);

    // Delete by variant id
    void deleteByBienThe_Id(Long idBienThe);

    @Query(value = """
            SELECT sp.id AS product_id,
                   sp.ten AS product_name,
                   SUM(ct.so_luong) AS total_sold,
                   SUM(ct.thanh_tien) AS total_revenue
            FROM don_hang_chi_tiet ct
            JOIN don_hang dh ON dh.id = ct.id_don_hang
            JOIN bien_the_san_pham bt ON bt.id = ct.id_bien_the
            JOIN san_pham sp ON sp.id = bt.id_san_pham
            WHERE dh.trang_thai = N'Hoàn tất'
              AND dh.dat_luc BETWEEN :from AND :to
            GROUP BY sp.id, sp.ten
            ORDER BY total_sold DESC
            """, nativeQuery = true)
    List<Object[]> findTopProductsBetween(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);

    @Query(value = """
            SELECT dm.id AS category_id,
                   dm.ten AS category_name,
                   SUM(ct.so_luong) AS total_sold,
                   SUM(ct.thanh_tien) AS total_revenue
            FROM don_hang_chi_tiet ct
            JOIN don_hang dh ON dh.id = ct.id_don_hang
            JOIN bien_the_san_pham bt ON bt.id = ct.id_bien_the
            JOIN san_pham sp ON sp.id = bt.id_san_pham
            JOIN danh_muc dm ON dm.id = sp.id_danh_muc
            WHERE dh.trang_thai = N'Hoàn tất'
              AND dh.dat_luc BETWEEN :from AND :to
            GROUP BY dm.id, dm.ten
            ORDER BY total_revenue DESC
            """, nativeQuery = true)
    List<Object[]> findCategoryPerformanceBetween(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to,
                                                  Pageable pageable);
}
