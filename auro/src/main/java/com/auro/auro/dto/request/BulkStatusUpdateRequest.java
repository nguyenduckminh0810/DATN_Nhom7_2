package com.auro.auro.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BulkStatusUpdateRequest {

    // Danh sách ID tài khoản cần cập nhật
    private List<Long> ids;

    // Trạng thái mới: true = active, false = inactive/ban
    private Boolean trangThai;
}


