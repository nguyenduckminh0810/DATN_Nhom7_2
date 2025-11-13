package com.auro.auro.repository;

import com.auro.auro.model.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Long> {
        Optional<DonHang> findBySoDonHang(String soDonHang);

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

        @Query("SELECT COALESCE(SUM(dh.tongThanhToan), 0) FROM DonHang dh WHERE dh.trangThai = 'Hoàn tất' AND dh.datLuc BETWEEN :from AND :to")
        BigDecimal sumRevenueByDatLucBetweenForCompleted(LocalDateTime from, LocalDateTime to);

        @Query(value = """
                        SELECT CAST(dh.dat_luc AS date) AS ngay,
                               SUM(dh.tong_thanh_toan) AS doanh_thu
                        FROM don_hang dh
                        WHERE dh.trang_thai = N'Hoàn tất'
                          AND dh.dat_luc BETWEEN :from AND :to
                        GROUP BY CAST(dh.dat_luc AS date)
                        ORDER BY ngay
                        """, nativeQuery = true)
        List<Object[]> sumRevenueByDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

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
                        WHERE dh.trang_thai = N'Hoàn tất'
                        """, nativeQuery = true)
        long countCustomersWithCompletedOrders();

        @Query(value = """
                        SELECT COUNT(*) FROM (
                            SELECT dh.id_khach_hang, COUNT(*) AS so_don
                            FROM don_hang dh
                            WHERE dh.trang_thai = N'Hoàn tất'
                            GROUP BY dh.id_khach_hang
                            HAVING COUNT(*) > 1
                        ) t
                        """, nativeQuery = true)
        long countRepeatCustomers();

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

}
