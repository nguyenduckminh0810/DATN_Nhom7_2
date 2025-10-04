package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_lieu")
@Data @NoArgsConstructor @AllArgsConstructor
public class ChatLieu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten", length = 100)
    private String ten;
}
