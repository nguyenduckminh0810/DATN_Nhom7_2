package com.auro.auro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
    
    private Long id;
    private String ma;
    private String loai;
    private BigDecimal giaTri;
    private BigDecimal giamToiDa;
    private BigDecimal donToiThieu;
    private LocalDateTime batDauLuc;
    private LocalDateTime ketThucLuc;
    private Integer gioiHanSuDung;
    private Integer trangThai;
    private Long usedCount;
    private BigDecimal totalDiscount;

    private String moTa;
    private Boolean daHetHan;
    private Boolean daHetLuot;
    private Boolean coTheApDung;
}
