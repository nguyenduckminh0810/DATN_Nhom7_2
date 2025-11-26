package com.auro.auro.repository;

import com.auro.auro.model.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    Optional<DanhMuc> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT d FROM DanhMuc d WHERE d.hoatDong = 1 ORDER BY d.thuTu ASC")
    List<DanhMuc> findActiveOrderByThuTuAsc();

    List<DanhMuc> findByDanhMucCha_Id(Long idCha);
}
