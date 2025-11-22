package com.auro.auro.controller;

import com.auro.auro.model.DonHang;
import com.auro.auro.repository.DonHangRepository;
import com.auro.auro.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/thong-ke")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonHangThongKeController {

    private final DonHangRepository donHangRepository;
    private final KhachHangRepository khachHangRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> result = new HashMap<>();

        // Tổng số đơn hàng
        long totalOrders = donHangRepository.count();

        // Tổng số khách hàng
        long totalCustomers = khachHangRepository.count();

        // Doanh thu hôm nay
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now();
        BigDecimal revenueToday = donHangRepository.sumRevenueByTaoLucBetween(startOfToday, endOfToday);

        // Đơn hàng mới 24h
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        long newOrders24h = donHangRepository.countByTaoLucAfter(last24h);

        result.put("totalOrders", totalOrders);
        result.put("totalCustomers", totalCustomers);
        result.put("revenueToday", revenueToday != null ? revenueToday.doubleValue() : 0.0);
        result.put("newOrders24h", newOrders24h);
        result.put("newCustomersToday", 0); // Placeholder
        result.put("lowStockCount", 0); // Placeholder

        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/summary")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getCustomerSummary() {
        Map<String, Object> result = new HashMap<>();
        long totalCustomers = khachHangRepository.count();

        result.put("totalCustomers", totalCustomers);
        result.put("activeCustomers", totalCustomers);
        result.put("newCustomers", 0);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders/status-counts")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getOrderStatusCounts() {
        Map<String, Object> result = new HashMap<>();

        // Lấy tất cả đơn hàng và đếm theo trạng thái
        List<DonHang> allOrders = donHangRepository.findAll();
        Map<String, Long> statusCounts = allOrders.stream()
                .collect(Collectors.groupingBy(
                        dh -> dh.getTrangThai() != null ? dh.getTrangThai() : "UNKNOWN",
                        Collectors.counting()));

        result.put("statusCounts", statusCounts);
        result.put("total", allOrders.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> result = new HashMap<>();
        result.put("lowStockProducts", Collections.emptyList());
        result.put("pendingOrders", donHangRepository.countByTrangThai("CHO_XAC_NHAN"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getChart(
            @RequestParam(defaultValue = "30days") String range,
            @RequestParam(defaultValue = "revenue") String metric) {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", Arrays.asList("Day 1", "Day 2", "Day 3"));
        result.put("data", Arrays.asList(100, 200, 150));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/categories/performance")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getCategoryPerformance() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/analytics/kpis")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getAnalyticsKpis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", donHangRepository.count());
        result.put("totalRevenue", 0);
        result.put("averageOrderValue", 0);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/business-insights")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getBusinessInsights(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("insights", Collections.emptyList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/hourly-sales")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getHourlySales(
            @RequestParam(required = false) String date) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/analytics/performance")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("metrics", Collections.emptyMap());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/order-distribution")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getOrderDistribution(
            @RequestParam(defaultValue = "status") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("distribution", Collections.emptyMap());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/customer-analytics")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics(
            @RequestParam(defaultValue = "segments") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("analytics", Collections.emptyMap());
        return ResponseEntity.ok(result);
    }

    // Fix payment status cho đơn hàng đã hoàn tất nhưng vẫn pending
    @PostMapping("/fix-payment-status")
    @PreAuthorize("hasAnyRole('ADM')")
    public ResponseEntity<Map<String, Object>> fixPaymentStatus() {
        List<DonHang> completedOrders = donHangRepository.findAll().stream()
                .filter(dh -> {
                    String status = dh.getTrangThai();
                    String paymentStatus = dh.getPaymentStatus();
                    String paymentMethod = dh.getPaymentMethod();

                    // Tìm đơn đã hoàn tất, COD, nhưng vẫn pending
                    boolean isCompleted = status != null &&
                            (status.contains("Hoàn") || status.contains("Giao")
                                    || status.equalsIgnoreCase("HOAN_THANH"));
                    boolean isCOD = "cod".equalsIgnoreCase(paymentMethod);
                    boolean isPending = paymentStatus != null &&
                            (paymentStatus.equalsIgnoreCase("pending") || paymentStatus.contains("Chờ"));

                    return isCompleted && isCOD && isPending;
                })
                .collect(Collectors.toList());

        int fixed = 0;
        for (DonHang dh : completedOrders) {
            dh.setPaymentStatus("PAID");
            donHangRepository.save(dh);
            fixed++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Đã sửa trạng thái thanh toán");
        result.put("fixed", fixed);
        result.put("total", completedOrders.size());

        return ResponseEntity.ok(result);
    }
}
