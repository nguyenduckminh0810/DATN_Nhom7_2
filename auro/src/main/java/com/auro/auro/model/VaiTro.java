package com.auro.auro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vai_tro")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma", nullable = false, unique = true)
    private String ma; // CUS, GST, STF, ADM

    @Column(name = "ten", nullable = false)
    private String ten; // Khách hàng, Khách vãng lai, Nhân viên, Quản trị viên
}