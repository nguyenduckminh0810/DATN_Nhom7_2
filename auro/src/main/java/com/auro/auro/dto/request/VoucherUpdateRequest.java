package com.auro.auro.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherUpdateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher không được quá 50 ký tự")
    private String ma;

    @NotBlank(message = "Loại voucher không được để trống")
    @Pattern(regexp = "percent|fixed|freeship|PHAN_TRAM|SO_TIEN", message = "Loại voucher phải là percent, fixed hoặc freeship")
    private String loai;

    @NotNull(message = "Giá trị voucher không được để trống")
    @DecimalMin(value = "0.00", message = "Giá trị voucher không được âm")
    private BigDecimal giaTri;

    private BigDecimal giamToiDa;

    private BigDecimal donToiThieu;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime batDauLuc;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime ketThucLuc;

    private Integer gioiHanSuDung;

    private String dieuKienJson;
}
