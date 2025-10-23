package com.auro.auro.repository;

import com.auro.auro.model.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Modifying
    @Query("DELETE FROM HinhAnh h WHERE h.sanPham.id = :sanPhamId")
    void deleteBySanPham_Id(@Param("sanPhamId") Long idSanPham);

    // Xóa ảnh theo biến thể
    @Modifying
    @Query("DELETE FROM HinhAnh h WHERE h.bienThe.id = :bienTheId")
    void deleteByBienThe_Id(@Param("bienTheId") Long idBienThe);

    // Xóa ảnh theo nhiều biến thể
    @Modifying
    @Query("DELETE FROM HinhAnh h WHERE h.bienThe.id IN :bienTheIds")
    void deleteByBienThe_IdIn(@Param("bienTheIds") Iterable<Long> bienTheIds);

    // Xóa ảnh theo nhiều sản phẩm
    @Modifying
    @Query("DELETE FROM HinhAnh h WHERE h.sanPham.id IN :sanPhamIds")
    void deleteBySanPham_IdIn(@Param("sanPhamIds") Iterable<Long> sanPhamIds);
}