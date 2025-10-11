package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DanhMucResponse {
    private Long id;
    private String ten;
    private String slug;
}
