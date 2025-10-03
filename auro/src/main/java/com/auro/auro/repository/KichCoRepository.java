package com.auro.auro.repository;

import com.auro.auro.model.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KichCoRepository extends JpaRepository<KichCo, Long> {
    Optional<KichCo> findByTen(String ten);

    boolean existsByTen(String ten);
}
