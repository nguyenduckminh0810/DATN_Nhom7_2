package com.auro.auro.repository;

import com.auro.auro.model.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, Long> {
    Optional<ChatLieu> findByTen(String ten);

    boolean existsByTen(String ten);
}
