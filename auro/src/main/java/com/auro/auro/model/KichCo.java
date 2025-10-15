package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kich_co")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KichCo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten", columnDefinition = "NVARCHAR(50)")
    private String ten;

    @Column(name = "thu_tu")
    private Integer thuTu;
}
