package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reset_token", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_tai_khoan", columnList = "id_tai_khoan")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;

    @Column(name = "het_han_luc", nullable = false)
    private LocalDateTime hetHanLuc;

    @Column(name = "da_su_dung", nullable = false)
    @Builder.Default
    private Boolean daSuDung = false;

    @Column(name = "tao_luc", nullable = false)
    private LocalDateTime taoLuc;

    @PrePersist
    protected void onCreate() {
        if (taoLuc == null) {
            taoLuc = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(hetHanLuc);
    }

    public boolean isValid() {
        return !daSuDung && !isExpired();
    }
}

