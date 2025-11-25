package com.auro.auro.repository;

import com.auro.auro.model.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import com.auro.auro.repository.projection.VoucherUsageProjection;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Long> {
        Optional<DonHang> findBySoDonHang(String soDonHang);

        List<DonHang> findTop5ByKhachHang_SoDienThoaiOrderByDatLucDesc(String soDienThoai);

        List<DonHang> findTop5ByKhachHang_SoDienThoaiInOrderByDatLucDesc(Collection<String> soDienThoais);

        Page<DonHang> findByKhachHang_Id(Long idKhachHang, Pageable pageable);

        @Query("SELECT DISTINCT dh FROM DonHang dh " +
                        "LEFT JOIN FETCH dh.chiTietList ct " +
                        "LEFT JOIN FETCH ct.bienThe bt " +
                        "LEFT JOIN FETCH bt.sanPham sp " +
                        "WHERE dh.khachHang.id = :khachHangId " +
                        "ORDER BY dh.taoLuc DESC")
        List<DonHang> findByKhachHang_IdWithDetails(Long khachHangId);

        Page<DonHang> findByTrangThai(String trangThai, Pageable pageable);

        Page<DonHang> findByDatLucBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

        @Query("SELECT dh FROM DonHang dh LEFT JOIN FETCH dh.chiTietList")
        List<DonHang> findAllWithChiTiet();

        Optional<DonHang> findByIdAndKhachHang_Id(Long id, Long khachHangId);

        long countByTaoLucAfter(LocalDateTime after);

        long countByTaoLucBetween(LocalDateTime from, LocalDateTime to);

        long countByTrangThai(String trangThai);

        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0) FROM DonHang dh WHERE dh.trangThai = :trangThai AND dh.datLuc BETWEEN :from AND :to")
        BigDecimal sumRevenueByDatLucBetweenAndTrangThai(@Param("trangThai") String trangThai,
                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0) FROM DonHang dh WHERE dh.datLuc BETWEEN :from AND :to")
        BigDecimal sumRevenueByDatLucBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0) FROM DonHang dh WHERE dh.trangThai = :trangThai")
        BigDecimal sumRevenueByTrangThai(@Param("trangThai") String trangThai);

        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0) FROM DonHang dh WHERE dh.taoLuc BETWEEN :from AND :to")
        BigDecimal sumRevenueByTaoLucBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query(value = """
                        SELECT CAST(dh.dat_luc AS date) AS ngay,
                               SUM(dh.tong_thanh_toan) AS doanh_thu
                        FROM don_hang dh
                        WHERE (dh.trang_thai = :trangThai
                               OR dh.trang_thai = N'Hoàn tất'
                               OR dh.trang_thai = N'Đã giao'
                               OR dh.trang_thai = 'COMPLETED'
                               OR dh.trang_thai = 'DELIVERED')
                          AND dh.dat_luc BETWEEN :from AND :to
                        GROUP BY CAST(dh.dat_luc AS date)
                        ORDER BY ngay
                        """, nativeQuery = true)
        List<Object[]> sumRevenueByDateBetween(@Param("trangThai") String trangThai,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);

        @Query(value = """
                        SELECT CAST(dh.dat_luc AS date) AS ngay,
                               COUNT(*) AS so_don
                        FROM don_hang dh
                        WHERE dh.dat_luc BETWEEN :from AND :to
                        GROUP BY CAST(dh.dat_luc AS date)
                        ORDER BY ngay
                        """, nativeQuery = true)
        List<Object[]> countOrdersByDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query(value = """
                        SELECT COUNT(DISTINCT dh.id_khach_hang)
                        FROM don_hang dh
                        WHERE dh.trang_thai = :trangThai
                        """, nativeQuery = true)
        long countCustomersWithCompletedOrders(@Param("trangThai") String trangThai);

        @Query(value = """
                        SELECT COUNT(*) FROM (
                            SELECT dh.id_khach_hang, COUNT(*) AS so_don
                            FROM don_hang dh
                            WHERE dh.trang_thai = :trangThai
                            GROUP BY dh.id_khach_hang
                            HAVING COUNT(*) > 1
                        ) t
                        """, nativeQuery = true)
        long countRepeatCustomers(@Param("trangThai") String trangThai);

        @Query("SELECT dh FROM DonHang dh LEFT JOIN FETCH dh.khachHang kh ORDER BY dh.taoLuc DESC")
        List<DonHang> findRecentOrders(Pageable pageable);

        // Analytics methods
        long countByDatLucBetween(LocalDateTime from, LocalDateTime to);

        long countByTrangThaiAndDatLucBetween(String trangThai, LocalDateTime from, LocalDateTime to);

        @Query(value = """
                        SELECT COUNT(DISTINCT dh.id_khach_hang)
                        FROM don_hang dh
                        WHERE dh.id_khach_hang IN (
                            SELECT id_khach_hang
                            FROM don_hang
                            GROUP BY id_khach_hang
                            HAVING COUNT(*) > 1
                        )
                        """, nativeQuery = true)
        long countCustomersWithMultipleOrders();

        @Query(value = """
                        SELECT COUNT(DISTINCT dh.id_khach_hang)
                        FROM don_hang dh
                        WHERE dh.tao_luc BETWEEN :from AND :to
                        """, nativeQuery = true)
        long countDistinctCustomersByTaoLucBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query(value = """
                        SELECT COUNT(DISTINCT dh.id_khach_hang)
                        FROM don_hang dh
                        WHERE dh.dat_luc BETWEEN :from AND :to
                        """, nativeQuery = true)
        long countDistinctCustomersByDatLucBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        // Count orders by customer
        @Query("SELECT COUNT(dh) FROM DonHang dh WHERE dh.khachHang.id = :khachHangId")
        int countByKhachHangId(@Param("khachHangId") Long khachHangId);

        // Sum total spent by customer (all orders)
        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0.0) FROM DonHang dh WHERE dh.khachHang.id = :khachHangId")
        Double sumTotalSpentByKhachHangId(@Param("khachHangId") Long khachHangId);

        // ============================================
        // NEW: Support both Vietnamese and English status values
        // ============================================

        @Query("SELECT COUNT(dh) FROM DonHang dh WHERE dh.trangThai IN :statusValues")
        long countByTrangThaiIn(@Param("statusValues") java.util.List<String> statusValues);

        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0) FROM DonHang dh WHERE dh.trangThai IN :statusValues AND dh.datLuc BETWEEN :from AND :to")
        BigDecimal sumRevenueByDatLucBetweenAndTrangThaiIn(@Param("statusValues") java.util.List<String> statusValues,
                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query("SELECT COUNT(dh) FROM DonHang dh WHERE dh.trangThai IN :statusValues AND dh.datLuc BETWEEN :from AND :to")
        long countByTrangThaiInAndDatLucBetween(@Param("statusValues") java.util.List<String> statusValues,
                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query(value = """
                        SELECT COUNT(DISTINCT dh.id_khach_hang)
                        FROM don_hang dh
                        WHERE dh.trang_thai IN :statusValues
                        """, nativeQuery = true)
        long countCustomersWithCompletedOrdersIn(@Param("statusValues") java.util.List<String> statusValues);

        @Query(value = """
                        SELECT COUNT(*) FROM (
                            SELECT dh.id_khach_hang, COUNT(*) AS so_don
                            FROM don_hang dh
                            WHERE dh.trang_thai IN :statusValues
                            GROUP BY dh.id_khach_hang
                            HAVING COUNT(*) > 1
                        ) t
                        """, nativeQuery = true)
        long countRepeatCustomersIn(@Param("statusValues") java.util.List<String> statusValues);

        @Query("""
                SELECT dh.voucher.ma AS voucherCode,
                       COUNT(dh.id) AS usageCount,
                       COALESCE(SUM(dh.giamGiaTong), 0) AS totalDiscount
                FROM DonHang dh
                WHERE dh.voucher IS NOT NULL AND dh.trangThai IN :statusValues
                GROUP BY dh.voucher.ma
                """)
        List<VoucherUsageProjection> aggregateVoucherUsage(@Param("statusValues") java.util.List<String> statusValues);

}