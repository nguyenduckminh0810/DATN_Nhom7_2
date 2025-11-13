package com.auro.auro.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DanhGiaDonHangRequest {

    @NotNull
    private Long chiTietId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer soSao;

    private String noiDung;
}

