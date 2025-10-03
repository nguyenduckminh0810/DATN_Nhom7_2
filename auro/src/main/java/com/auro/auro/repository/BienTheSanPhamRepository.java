package com.auro.auro.repository;

import com.auro.auro.model.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {
    List<BienTheSanPham> findBySanPham_Id(Long idSanPham);

    boolean existsBySku(String sku);

    Optional<BienTheSanPham> findBySanPham_IdAndMauSac_IdAndKichCo_IdAndChatLieu_Id(
            Long idSanPham, Long idMauSac, Long idKichCo, Long idChatLieu);
}
