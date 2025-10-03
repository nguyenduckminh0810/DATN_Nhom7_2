package com.auro.auro.repository;

import com.auro.auro.model.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HinhAnhRepository extends JpaRepository<HinhAnh, Long> {
    // Ảnh theo sản phẩm
    List<HinhAnh> findBySanPham_IdOrderByThuTuAscIdAsc(Long idSanPham);

    // Ảnh theo biến thể
    List<HinhAnh> findByBienThe_IdOrderByThuTuAscIdAsc(Long idBienThe);

    // Ảnh đại diện của sản phẩm
    Optional<HinhAnh> findFirstBySanPham_IdOrderByThuTuAscIdAsc(Long idSanPham);

    // Ảnh đại diện của biến thể
    Optional<HinhAnh> findFirstByBienThe_IdOrderByThuTuAscIdAsc(Long idBienThe);

    // Xóa ảnh theo sản phẩm
    void deleteBySanPham_Id(Long idSanPham);

    // Xóa ảnh theo biến thể
    void deleteByBienThe_Id(Long idBienThe);
}