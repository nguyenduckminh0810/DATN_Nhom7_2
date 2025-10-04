package com.auro.auro.repository;

import com.auro.auro.model.VoucherKhach;
import com.auro.auro.model.VoucherKhachId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherKhachRepository extends JpaRepository<VoucherKhach, VoucherKhachId> {

    // Tìm theo composite key: (id_voucher, id_khach_hang)
    Optional<VoucherKhach> findById(VoucherKhachId id);

    // List voucher của một khách
    List<VoucherKhach> findByKhachHang_Id(Long idKhachHang);

    // Kiểm tra khách đã dùng voucher này chưa
    boolean existsByIdAndTrangThai(VoucherKhachId id, String trangThai);

    // Lấy record theo trạng thái (UNUSED/USED/LOCKED...) để xử lý
    Optional<VoucherKhach> findByIdAndTrangThai(VoucherKhachId id, String trangThai);
}
