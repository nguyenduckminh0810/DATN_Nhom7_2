package com.auro.auro.repository;
import com.auro.auro.model.ThanhToanVanChuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ThanhToanVanChuyenRepository extends JpaRepository<ThanhToanVanChuyen, Long> {

    // Tìm theo đơn hàng
    Optional<ThanhToanVanChuyen> findByDonHang_Id(Long idDonHang);

    // Tìm theo mã giao dịch
    Optional<ThanhToanVanChuyen> findByMaGiaoDich(String maGiaoDich);

    // Tìm theo mã vận đơn
    Optional<ThanhToanVanChuyen> findByMaVanDon(String maVanDon);

    // Tìm theo trạng thái thanh toán
    List<ThanhToanVanChuyen> findByTtThanhToan(String ttThanhToan);

    // Tìm theo trạng thái vận chuyển
    List<ThanhToanVanChuyen> findByTtVanChuyen(String ttVanChuyen);

    // Tìm đơn cần giao
    @Query("select ttv from ThanhToanVanChuyen ttv where ttv.ttThanhToan = 'paid' and ttv.ttVanChuyen IN ('preparing', 'shipping')")
    List<ThanhToanVanChuyen> findOrdersToDeliver();

    // Tìm đơn đã giao thành công
    @Query("select ttv from ThanhToanVanChuyen ttv where ttv.ttVanChuyen = 'delivered' and ttv.giaoThanhCongLuc is not null")
    List<ThanhToanVanChuyen> findDeliveredOrders();
}
