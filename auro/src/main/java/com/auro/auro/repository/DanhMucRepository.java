package com.auro.auro.repository;

import com.auro.auro.model.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    Optional<DanhMuc> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<DanhMuc> findAllByHoatDongTrueOrderByThuTuAsc();

    List<DanhMuc> findByDanhMucCha_Id(Long idCha);
}
