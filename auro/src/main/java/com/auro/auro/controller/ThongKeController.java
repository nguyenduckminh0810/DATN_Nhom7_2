// package com.auro.auro.controller;

// import com.auro.auro.service.ThongKeService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/thong-ke")
// @CrossOrigin(origins = "*")
// @RequiredArgsConstructor
// public class ThongKeController {

// private final ThongKeService thongKeService;

// @GetMapping("/summary")
// public ResponseEntity<Map<String, Object>> summary(
// @RequestParam(name = "lowStockThreshold", required = false) Integer
// lowStockThreshold) {
// return ResponseEntity.ok(thongKeService.getSummary(lowStockThreshold));
// }

// @GetMapping("/alerts")
// public ResponseEntity<Map<String, Object>> alerts(
// @RequestParam(name = "lowStockThreshold", required = false) Integer
// lowStockThreshold) {
// return ResponseEntity.ok(thongKeService.getAlerts(lowStockThreshold));
// }

// @GetMapping("/chart")
// public ResponseEntity<Map<String, Object>> chart(
// @RequestParam(name = "range", defaultValue = "30days") String range,
// @RequestParam(name = "metric", defaultValue = "revenue") String metric) {
// return ResponseEntity.ok(thongKeService.getChart(range, metric));
// }

// @GetMapping("/top-products")
// public ResponseEntity<List<Map<String, Object>>> topProducts(
// @RequestParam(name = "limit", required = false) Integer limit,
// @RequestParam(name = "rangeDays", required = false) Integer rangeDays) {
// return ResponseEntity.ok(thongKeService.getTopProducts(limit, rangeDays));
// }

// @GetMapping("/recent-orders")
// public ResponseEntity<List<Map<String, Object>>> recentOrders(
// @RequestParam(name = "limit", required = false) Integer limit) {
// return ResponseEntity.ok(thongKeService.getRecentOrders(limit));
// }

// @GetMapping("/customers/summary")
// public ResponseEntity<Map<String, Object>> customerSummary() {
// return ResponseEntity.ok(thongKeService.getCustomerSummary());
// }

// @GetMapping("/categories/performance")
// public ResponseEntity<List<Map<String, Object>>> categoryPerformance(
// @RequestParam(name = "limit", required = false) Integer limit,
// @RequestParam(name = "rangeDays", required = false) Integer rangeDays) {
// return ResponseEntity.ok(thongKeService.getCategoryPerformance(limit,
// rangeDays));
// }

// @GetMapping("/orders/status-counts")
// public ResponseEntity<Map<String, Object>> orderStatusCounts() {
// return ResponseEntity.ok(thongKeService.getOrderStatusCounts());
// }

// @GetMapping("/analytics/kpis")
// public ResponseEntity<Map<String, Object>> analyticsKpis(
// @RequestParam(name = "startDate", required = false) String startDate,
// @RequestParam(name = "endDate", required = false) String endDate) {
// return ResponseEntity.ok(thongKeService.getAnalyticsKpis(startDate,
// endDate));
// }

// @GetMapping("/analytics/business-insights")
// public ResponseEntity<Map<String, Object>> businessInsights(
// @RequestParam(name = "startDate", required = false) String startDate,
// @RequestParam(name = "endDate", required = false) String endDate) {
// return ResponseEntity.ok(thongKeService.getBusinessInsights(startDate,
// endDate));
// }

// @GetMapping("/analytics/hourly-sales")
// public ResponseEntity<List<Map<String, Object>>> hourlySales(
// @RequestParam(name = "date", required = false) String date) {
// return ResponseEntity.ok(thongKeService.getHourlySales(date));
// }

// @GetMapping("/analytics/performance")
// public ResponseEntity<Map<String, Object>> performanceMetrics(
// @RequestParam(name = "startDate", required = false) String startDate,
// @RequestParam(name = "endDate", required = false) String endDate) {
// return ResponseEntity.ok(thongKeService.getPerformanceMetrics(startDate,
// endDate));
// }

// @GetMapping("/analytics/order-distribution")
// public ResponseEntity<Map<String, Object>> orderDistribution(
// @RequestParam(name = "type", defaultValue = "status") String type,
// @RequestParam(name = "startDate", required = false) String startDate,
// @RequestParam(name = "endDate", required = false) String endDate) {
// return ResponseEntity.ok(thongKeService.getOrderDistribution(type, startDate,
// endDate));
// }

// @GetMapping("/analytics/customer-analytics")
// public ResponseEntity<Map<String, Object>> customerAnalytics(
// @RequestParam(name = "type", defaultValue = "segments") String type,
// @RequestParam(name = "startDate", required = false) String startDate,
// @RequestParam(name = "endDate", required = false) String endDate) {
// return ResponseEntity.ok(thongKeService.getCustomerAnalytics(type, startDate,
// endDate));
// }
// }
