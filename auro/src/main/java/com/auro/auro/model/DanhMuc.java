package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "danh_muc")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String ten;

    @Column(name = "slug", nullable = false, columnDefinition = "NVARCHAR(200)", unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cha")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DanhMuc danhMucCha;

    @Column(name = "thu_tu")
    private Integer thuTu;

    @Column(name = "hoat_dong")
    private Integer hoatDong = 1;
}
