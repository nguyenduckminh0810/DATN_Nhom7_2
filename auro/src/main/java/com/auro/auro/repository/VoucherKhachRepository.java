package com.auro.auro.repository;

import com.auro.auro.model.VoucherKhach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherKhachRepository extends JpaRepository<VoucherKhach, Long> {

    // List voucher của một khách
    List<VoucherKhach> findByKhachHang_Id(Long idKhachHang);
}
