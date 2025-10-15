package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mau_sac")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten", columnDefinition = "NVARCHAR(100)")
    private String ten;

    @Column(name = "ma", columnDefinition = "NVARCHAR(20)") // mã hex hoặc code
    private String ma;
}
