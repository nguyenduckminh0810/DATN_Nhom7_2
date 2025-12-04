package com.auro.auro.controller;

import com.auro.auro.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/thong-ke")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonHangThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> summary(
            @RequestParam(name = "lowStockThreshold", required = false) Integer lowStockThreshold) {
        return ResponseEntity.ok(thongKeService.getSummary(lowStockThreshold));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> alerts(
            @RequestParam(name = "lowStockThreshold", required = false) Integer lowStockThreshold) {
        return ResponseEntity.ok(thongKeService.getAlerts(lowStockThreshold));
    }

    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> chart(
            @RequestParam(name = "range", defaultValue = "30days") String range,
            @RequestParam(name = "metric", defaultValue = "revenue") String metric) {
        return ResponseEntity.ok(thongKeService.getChart(range, metric));
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> topProducts(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "rangeDays", required = false) Integer rangeDays) {
        return ResponseEntity.ok(thongKeService.getTopProducts(limit, rangeDays));
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> recentOrders(
            @RequestParam(name = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(thongKeService.getRecentOrders(limit));
    }

    @GetMapping("/customers/summary")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> customerSummary() {
        return ResponseEntity.ok(thongKeService.getCustomerSummary());
    }

    @GetMapping("/categories/performance")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> categoryPerformance(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "rangeDays", required = false) Integer rangeDays) {
        return ResponseEntity.ok(thongKeService.getCategoryPerformance(limit,
                rangeDays));
    }

    @GetMapping("/orders/status-counts")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> orderStatusCounts() {
        return ResponseEntity.ok(thongKeService.getOrderStatusCounts());
    }

    @GetMapping("/analytics/kpis")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> analyticsKpis(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        return ResponseEntity.ok(thongKeService.getAnalyticsKpis(startDate,
                endDate));
    }

    @GetMapping("/analytics/business-insights")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> businessInsights(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        return ResponseEntity.ok(thongKeService.getBusinessInsights(startDate,
                endDate));
    }

    @GetMapping("/analytics/hourly-sales")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<List<Map<String, Object>>> hourlySales(
            @RequestParam(name = "date", required = false) String date) {
        return ResponseEntity.ok(thongKeService.getHourlySales(date));
    }

    @GetMapping("/analytics/performance")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> performanceMetrics(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        return ResponseEntity.ok(thongKeService.getPerformanceMetrics(startDate,
                endDate));
    }

    @GetMapping("/analytics/order-distribution")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> orderDistribution(
            @RequestParam(name = "type", defaultValue = "status") String type,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        return ResponseEntity.ok(thongKeService.getOrderDistribution(type, startDate,
                endDate));
    }

    @GetMapping("/analytics/customer-analytics")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<Map<String, Object>> customerAnalytics(
            @RequestParam(name = "type", defaultValue = "segments") String type,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        return ResponseEntity.ok(thongKeService.getCustomerAnalytics(type, startDate,
                endDate));
    }
}
