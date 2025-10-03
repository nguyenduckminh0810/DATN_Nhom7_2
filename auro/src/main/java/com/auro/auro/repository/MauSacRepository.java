package com.auro.auro.repository;

import com.auro.auro.model.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MauSacRepository extends JpaRepository<MauSac, Long> {
    Optional<MauSac> findByTen(String ten);

    boolean existsByTen(String ten);
}
