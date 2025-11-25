package com.auro.auro.controller;

import com.auro.auro.model.DonHang;
import com.auro.auro.repository.DonHangRepository;
import com.auro.auro.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/thong-ke")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonHangThongKeController {

    private final DonHangRepository donHangRepository;
    private final ThongKeService thongKeService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(name = "lowStockThreshold", required = false) Integer lowStockThreshold) {
        // Sử dụng ThongKeService để có logic đầy đủ
        Map<String, Object> result = thongKeService.getSummary(lowStockThreshold);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/summary")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getCustomerSummary() {
        Map<String, Object> result = thongKeService.getCustomerSummary();
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

        System.out.println("📊 [ORDER STATUS COUNTS DEBUG]");
        System.out.println("  - Total Orders: " + allOrders.size());
        System.out.println("  - Status Counts Map: " + statusCounts);

        // Map các trạng thái về format chuẩn cho frontend
        long pending = 0;
        long shipping = 0;
        long delivered = 0;
        long completed = 0;
        long cancelled = 0;

        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            String status = entry.getKey();
            Long count = entry.getValue();
            
            System.out.println("  - Processing status: '" + status + "' with count: " + count);
            
            // Normalize status để so sánh (loại bỏ khoảng trắng, chuyển về uppercase)
            String normalizedStatus = (status != null && !status.trim().isEmpty()) 
                    ? status.trim().toUpperCase() : "";
            
            // Hỗ trợ cả tiếng Anh và tiếng Việt - kiểm tra exact match trước, sau đó mới dùng contains
            if (status != null && (status.equals("PENDING") || status.equals("Chờ xác nhận") || 
                status.equals("CHO_XAC_NHAN") || normalizedStatus.equals("PENDING") ||
                (normalizedStatus.contains("CHỜ") || normalizedStatus.contains("CHO")))) {
                pending += count;
                System.out.println("    → Mapped to PENDING");
            } else if (status != null && (status.equals("SHIPPING") || status.equals("Đang giao") || 
                      status.equals("DANG_GIAO") || normalizedStatus.equals("SHIPPING") ||
                      (normalizedStatus.contains("ĐANG") || normalizedStatus.contains("DANG")))) {
                shipping += count;
                System.out.println("    → Mapped to SHIPPING");
            } else if (status != null && (status.equals("DELIVERED") || status.equals("Đã giao") || 
                      status.equals("DA_GIAO") || status.equals("Đã giao hàng") ||
                      status.equals("DA_GIAO_HANG") || normalizedStatus.equals("DELIVERED") ||
                      (normalizedStatus.contains("ĐÃ GIAO") && !normalizedStatus.contains("HOÀN")))) {
                delivered += count;
                System.out.println("    → Mapped to DELIVERED");
            } else if (status != null && (status.equals("COMPLETED") || status.equals("Hoàn tất") || 
                      status.equals("HOAN_TAT") || status.equals("HOAN_THANH") ||
                      status.equals("Hoàn thành") || normalizedStatus.equals("COMPLETED") ||
                      (normalizedStatus.contains("HOÀN") || normalizedStatus.contains("HOAN")))) {
                completed += count;
                System.out.println("    → Mapped to COMPLETED");
            } else if (status != null && (status.equals("CANCELLED") || status.equals("Đã hủy") || 
                      status.equals("DA_HUY") || normalizedStatus.equals("CANCELLED") ||
                      (normalizedStatus.contains("HỦY") || normalizedStatus.contains("HUY")))) {
                cancelled += count;
                System.out.println("    → Mapped to CANCELLED");
            } else {
                System.out.println("    → UNMAPPED STATUS: " + status);
            }
        }
        
        System.out.println("  - Final counts:");
        System.out.println("    PENDING: " + pending);
        System.out.println("    SHIPPING: " + shipping);
        System.out.println("    DELIVERED: " + delivered);
        System.out.println("    COMPLETED: " + completed);
        System.out.println("    CANCELLED: " + cancelled);

        result.put("pending", pending);
        result.put("shipping", shipping);
        result.put("delivered", delivered);
        result.put("completed", completed);
        result.put("cancelled", cancelled);
        result.put("statusCounts", statusCounts); // Giữ lại để debug
        result.put("total", allOrders.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getAlerts(
            @RequestParam(name = "lowStockThreshold", required = false) Integer lowStockThreshold) {
        // Sử dụng ThongKeService để có logic đầy đủ
        Map<String, Object> result = thongKeService.getAlerts(lowStockThreshold);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getChart(
            @RequestParam(defaultValue = "30days") String range,
            @RequestParam(defaultValue = "revenue") String metric) {
        Map<String, Object> result = thongKeService.getChart(range, metric);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "rangeDays", required = false) Integer rangeDays) {
        List<Map<String, Object>> result = thongKeService.getTopProducts(limit, rangeDays);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders(
            @RequestParam(name = "limit", required = false) Integer limit) {
        List<Map<String, Object>> result = thongKeService.getRecentOrders(limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories/performance")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getCategoryPerformance(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "rangeDays", required = false) Integer rangeDays) {
        List<Map<String, Object>> result = thongKeService.getCategoryPerformance(limit, rangeDays);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/kpis")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getAnalyticsKpis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = thongKeService.getAnalyticsKpis(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/business-insights")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getBusinessInsights(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = thongKeService.getBusinessInsights(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/hourly-sales")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> getHourlySales(
            @RequestParam(required = false) String date) {
        List<Map<String, Object>> result = thongKeService.getHourlySales(date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/performance")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = thongKeService.getPerformanceMetrics(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/order-distribution")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getOrderDistribution(
            @RequestParam(defaultValue = "status") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = thongKeService.getOrderDistribution(type, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/customer-analytics")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics(
            @RequestParam(defaultValue = "segments") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = thongKeService.getCustomerAnalytics(type, startDate, endDate);
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
