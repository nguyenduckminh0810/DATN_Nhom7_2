package com.auro.auro.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangPageResponse {
    private List<DonHangResponse> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private Map<String, Long> statusCounts;
}

