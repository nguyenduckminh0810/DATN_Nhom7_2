package com.auro.auro.service;

import com.auro.auro.constants.OrderStatus;
import com.auro.auro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThongKeService {

    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final GioHangRepository gioHangRepository;

    public Map<String, Object> getSummary(Integer lowStockThreshold) {
        if (lowStockThreshold == null)
            lowStockThreshold = 10;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfYesterday = startOfToday.minusDays(1);
        LocalDateTime endOfYesterday = startOfToday.minusSeconds(1);

        // Today stats
        long ordersToday = donHangRepository.countByTaoLucBetween(startOfToday, now);
        long orders24h = donHangRepository.countByTaoLucBetween(now.minusHours(24), now);

        // Total revenue (all completed orders) - Changed from "today" to "total"
        // Because most orders have datLuc in the past, not today
        BigDecimal revenueToday = donHangRepository
                .sumRevenueByTrangThai(OrderStatus.HOAN_TAT);

        // Yesterday stats for comparison
        long ordersYesterday = donHangRepository.countByTaoLucBetween(startOfYesterday, endOfYesterday);
        BigDecimal revenueYesterday = donHangRepository.sumRevenueByDatLucBetweenAndTrangThai(OrderStatus.HOAN_TAT,
                startOfYesterday, endOfYesterday);

        // Calculate growth
        double orderGrowth = calculateGrowth(ordersToday, ordersYesterday);
        double revenueGrowth = calculateGrowth(revenueToday, revenueYesterday);

        // Customer stats
        long totalCustomers = khachHangRepository.count();
        long newCustomersToday = 0; // TODO: Need to add taoLuc field to KhachHang
        double customersGrowth = 0.0; // TODO: Calculate when taoLuc is available

        // Low stock count
        long lowStockCount = bienTheSanPhamRepository.countBySoLuongTonLessThanEqual(lowStockThreshold);

        Map<String, Object> summary = new HashMap<>();
        summary.put("revenueToday", revenueToday != null ? revenueToday : BigDecimal.ZERO);
        summary.put("revenueGrowth", Math.round(revenueGrowth * 100.0) / 100.0);
        summary.put("newOrders24h", orders24h);
        summary.put("ordersGrowth", Math.round(orderGrowth * 100.0) / 100.0);
        summary.put("newCustomersToday", newCustomersToday);
        summary.put("customersGrowth", Math.round(customersGrowth * 100.0) / 100.0);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalCustomers", totalCustomers);

        return summary;
    }

    public Map<String, Object> getAlerts(Integer lowStockThreshold) {
        if (lowStockThreshold == null)
            lowStockThreshold = 10;

        long lowStockCount = bienTheSanPhamRepository.countBySoLuongTonLessThanEqual(lowStockThreshold);
        long pendingOrders = donHangRepository.countByTrangThai(OrderStatus.CHO_XAC_NHAN);
        long shippingOrders = donHangRepository.countByTrangThai(OrderStatus.DANG_GIAO);

        Map<String, Object> alerts = new HashMap<>();
        alerts.put("lowStockProducts", lowStockCount);
        alerts.put("pendingOrders", pendingOrders);
        alerts.put("needShipping", shippingOrders);

        return alerts;
    }

    public Map<String, Object> getChart(String range, String metric) {
        LocalDateTime now = LocalDateTime.now();
        int days;

        switch (range) {
            case "7days" -> days = 7;
            case "30days" -> days = 30;
            case "90days" -> days = 90;
            case "1year" -> days = 365;
            default -> days = 30;
        }

        LocalDateTime currentStart = now.minusDays(days);
        LocalDateTime previousStart = currentStart.minusDays(days);
        LocalDateTime previousEnd = currentStart.minusSeconds(1);

        // Generate labels (dates)
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = currentStart.plusDays(i).toLocalDate();
            labels.add(date.toString());
        }

        // Get current period data
        Map<String, Number> currentDataMap = new HashMap<>();
        Map<String, Number> previousDataMap = new HashMap<>();

        if ("revenue".equals(metric)) {
            // Current period revenue
            List<Object[]> currentRevenue = donHangRepository.sumRevenueByDateBetween(
                    OrderStatus.HOAN_TAT, currentStart, now);
            for (Object[] row : currentRevenue) {
                currentDataMap.put(row[0].toString(), (Number) row[1]);
            }

            // Previous period revenue
            List<Object[]> previousRevenue = donHangRepository.sumRevenueByDateBetween(
                    OrderStatus.HOAN_TAT, previousStart, previousEnd);
            for (Object[] row : previousRevenue) {
                // Shift dates forward by 'days' to align with current period
                LocalDate oldDate = LocalDate.parse(row[0].toString());
                LocalDate newDate = oldDate.plusDays(days);
                previousDataMap.put(newDate.toString(), (Number) row[1]);
            }
        } else if ("orders".equals(metric)) {
            // Current period orders
            List<Object[]> currentOrders = donHangRepository.countOrdersByDateBetween(currentStart, now);
            for (Object[] row : currentOrders) {
                currentDataMap.put(row[0].toString(), (Number) row[1]);
            }

            // Previous period orders
            List<Object[]> previousOrders = donHangRepository.countOrdersByDateBetween(previousStart, previousEnd);
            for (Object[] row : previousOrders) {
                LocalDate oldDate = LocalDate.parse(row[0].toString());
                LocalDate newDate = oldDate.plusDays(days);
                previousDataMap.put(newDate.toString(), (Number) row[1]);
            }
        }

        // Build arrays aligned with labels
        List<Number> currentData = new ArrayList<>();
        List<Number> previousData = new ArrayList<>();

        for (String label : labels) {
            currentData.add(currentDataMap.getOrDefault(label, 0));
            previousData.add(previousDataMap.getOrDefault(label, 0));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("current", currentData);
        result.put("previous", previousData);

        return result;
    }

    public List<Map<String, Object>> getTopProducts(Integer limit, Integer rangeDays) {
        if (limit == null)
            limit = 5;
        if (rangeDays == null)
            rangeDays = 30;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(rangeDays);
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> topProducts = donHangChiTietRepository.findTopProductsBetween(OrderStatus.HOAN_TAT, startDate,
                now, pageable);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : topProducts) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", row[0]);
            product.put("name", row[1]);
            product.put("sales", row[2]);
            product.put("revenue", row[3]);
            product.put("image", null); // TODO: Add image URL from SanPham
            result.add(product);
        }

        return result;
    }

    public List<Map<String, Object>> getRecentOrders(Integer limit) {
        if (limit == null)
            limit = 10;

        Pageable pageable = PageRequest.of(0, limit);
        var orders = donHangRepository.findRecentOrders(pageable);

        return orders.stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderCode", order.getSoDonHang());
            orderMap.put("customer", order.getKhachHang() != null ? order.getKhachHang().getHoTen() : "Khách vãng lai");
            orderMap.put("amount", order.getTongThanhToan());
            orderMap.put("status", order.getTrangThai());
            orderMap.put("time", order.getDatLuc() != null ? order.getDatLuc().toString() : "");
            return orderMap;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getCustomerSummary() {
        long totalCustomers = khachHangRepository.count();
        long customersWithOrders = donHangRepository.countCustomersWithCompletedOrders(OrderStatus.HOAN_TAT);
        long repeatCustomers = donHangRepository.countRepeatCustomers(OrderStatus.HOAN_TAT);

        double repeatRate = totalCustomers > 0
                ? (double) repeatCustomers / totalCustomers * 100
                : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", totalCustomers);
        summary.put("customersWithOrders", customersWithOrders);
        summary.put("repeatCustomers", repeatCustomers);
        summary.put("repeatRate", Math.round(repeatRate * 100.0) / 100.0);

        return summary;
    }

    public List<Map<String, Object>> getCategoryPerformance(Integer limit, Integer rangeDays) {
        if (limit == null)
            limit = 5;
        if (rangeDays == null)
            rangeDays = 30;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(rangeDays);
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> categoryData = donHangChiTietRepository.findCategoryPerformanceBetween(OrderStatus.HOAN_TAT,
                startDate, now, pageable);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : categoryData) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", row[0]);
            category.put("name", row[1]);
            category.put("sales", row[2]);
            category.put("revenue", row[3]);
            category.put("change", 0); // TODO: Calculate change vs previous period
            category.put("trend", "up"); // TODO: Calculate trend
            category.put("trendIcon", "bi bi-graph-up"); // TODO: Set based on trend
            result.add(category);
        }

        return result;
    }

    public Map<String, Object> getOrderStatusCounts() {
        Map<String, Object> statusCounts = new HashMap<>();
        statusCounts.put("pending", donHangRepository.countByTrangThai(OrderStatus.CHO_XAC_NHAN));
        statusCounts.put("shipping", donHangRepository.countByTrangThai(OrderStatus.DANG_GIAO));
        statusCounts.put("completed", donHangRepository.countByTrangThai(OrderStatus.HOAN_TAT));
        statusCounts.put("cancelled", donHangRepository.countByTrangThai(OrderStatus.DA_HUY));

        return statusCounts;
    }

    public Map<String, Object> getAnalyticsKpis(String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseDate(startDateStr, LocalDate.now().minusDays(30));
        LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        // Calculate period length in days
        long periodDays = java.time.Duration.between(startDate, endDate).toDays();
        if (periodDays == 0)
            periodDays = 1;

        // Previous period for comparison
        LocalDateTime previousStart = startDate.minusDays(periodDays);
        LocalDateTime previousEnd = startDate.minusSeconds(1);

        // Current period stats
        long totalOrders = donHangRepository.countByDatLucBetween(startDate, endDate);
        long completedOrders = donHangRepository.countByTrangThaiAndDatLucBetween(OrderStatus.HOAN_TAT, startDate,
                endDate);
        BigDecimal totalRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThai(OrderStatus.HOAN_TAT,
                startDate, endDate);
        long totalCustomers = khachHangRepository.count();
        long totalProducts = sanPhamRepository.count();

        // Previous period stats for growth calculation
        long previousOrders = donHangRepository.countByDatLucBetween(previousStart, previousEnd);
        BigDecimal previousRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThai(OrderStatus.HOAN_TAT,
                previousStart, previousEnd);

        // Note: For customers and products, we can't track historical count easily
        // without timestamp fields
        // So we'll use a simple approximation or return 0 for now
        long previousCustomers = totalCustomers; // TODO: Need taoLuc field in KhachHang to calculate properly
        long previousProducts = totalProducts; // TODO: Need taoLuc field in SanPham to calculate properly

        // Calculate growth rates
        double revenueGrowth = calculateGrowth(totalRevenue, previousRevenue);
        double ordersGrowth = calculateGrowth(totalOrders, previousOrders);
        double customersGrowth = calculateGrowth(totalCustomers, previousCustomers);
        double productsGrowth = calculateGrowth(totalProducts, previousProducts);

        // Calculate other metrics
        BigDecimal avgOrderValue = totalOrders > 0 && totalRevenue != null
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        double completionRate = totalOrders > 0
                ? (double) completedOrders / totalOrders * 100
                : 0.0;

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalOrders", totalOrders);
        kpis.put("completedOrders", completedOrders);
        kpis.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        kpis.put("totalCustomers", totalCustomers);
        kpis.put("totalProducts", totalProducts);
        kpis.put("avgOrderValue", avgOrderValue);
        kpis.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        kpis.put("revenueGrowth", Math.round(revenueGrowth * 100.0) / 100.0);
        kpis.put("ordersGrowth", Math.round(ordersGrowth * 100.0) / 100.0);
        kpis.put("customersGrowth", Math.round(customersGrowth * 100.0) / 100.0);
        kpis.put("productsGrowth", Math.round(productsGrowth * 100.0) / 100.0);

        return kpis;
    }

    public Map<String, Object> getBusinessInsights(String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseDate(startDateStr, LocalDate.now().minusDays(30));
        LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        long newCustomers = khachHangRepository.count();
        long repeatCustomers = donHangRepository.countRepeatCustomers(OrderStatus.HOAN_TAT);
        long totalOrders = donHangRepository.countByDatLucBetween(startDate, endDate);
        BigDecimal totalRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThai(OrderStatus.HOAN_TAT,
                startDate, endDate);

        // Calculate additional metrics
        BigDecimal averageOrderValue = totalOrders > 0 && totalRevenue != null
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        double returningCustomersRate = newCustomers > 0
                ? (double) repeatCustomers / newCustomers * 100
                : 0.0;

        // Mock data for metrics that need more complex calculation
        // TODO: Implement real calculation when data is available
        double refundRate = 0.0; // TODO: Calculate from cancelled orders
        double profitMargin = 25.0; // TODO: Calculate from cost data
        BigDecimal customerLifetimeValue = averageOrderValue.multiply(BigDecimal.valueOf(2)); // Rough estimate
        int averageRetentionDays = 90; // TODO: Calculate from customer data

        // Get top selling product
        String topSellingProduct = "N/A";
        List<Map<String, Object>> topProducts = getTopProducts(1, 30);
        if (!topProducts.isEmpty() && topProducts.get(0).get("name") != null) {
            topSellingProduct = (String) topProducts.get(0).get("name");
        }

        // Get top category
        String topCategory = "N/A";
        List<Map<String, Object>> topCategories = getCategoryPerformance(1, 30);
        if (!topCategories.isEmpty() && topCategories.get(0).get("name") != null) {
            topCategory = (String) topCategories.get(0).get("name");
        }

        // Calculate inventory turnover (simplified: total sold quantity / total
        // inventory)
        double inventoryTurnover = 0.0;
        try {
            long totalSoldQuantity = donHangChiTietRepository.findTotalSoldQuantityBetween(
                    OrderStatus.HOAN_TAT, startDate, endDate);
            long totalInventory = bienTheSanPhamRepository.findTotalInventory();
            if (totalInventory > 0) {
                inventoryTurnover = (double) totalSoldQuantity / totalInventory;
            }
        } catch (Exception e) {
            // If query fails, keep 0.0
            inventoryTurnover = 0.0;
        }

        // Get today's metrics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

        // Try to get orders by taoLuc first, if no results, fallback to datLuc
        long todayOrders = donHangRepository.countByTaoLucBetween(startOfToday, now);
        BigDecimal todayRevenue = donHangRepository.sumRevenueByTaoLucBetween(startOfToday, now);
        long todayCustomers = donHangRepository.countDistinctCustomersByTaoLucBetween(startOfToday, now);

        // Fallback: If no orders found by taoLuc, try datLuc (for orders that might not
        // have taoLuc set)
        if (todayOrders == 0 || (todayRevenue != null && todayRevenue.compareTo(BigDecimal.ZERO) == 0)) {
            long ordersByDatLuc = donHangRepository.countByDatLucBetween(startOfToday, now);
            BigDecimal revenueByDatLuc = donHangRepository.sumRevenueByDatLucBetween(startOfToday, now);
            long customersByDatLuc = donHangRepository.countDistinctCustomersByDatLucBetween(startOfToday, now);

            // Use datLuc data if it has more orders
            if (ordersByDatLuc > todayOrders) {
                todayOrders = ordersByDatLuc;
                todayRevenue = revenueByDatLuc;
                todayCustomers = customersByDatLuc;
            }
        }

        Map<String, Object> insights = new HashMap<>();
        insights.put("newCustomers", newCustomers);
        insights.put("repeatCustomers", repeatCustomers);
        insights.put("totalOrders", totalOrders);
        insights.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        insights.put("averageOrderValue", averageOrderValue);
        insights.put("refundRate", Math.round(refundRate * 100.0) / 100.0);
        insights.put("profitMargin", Math.round(profitMargin * 100.0) / 100.0);
        insights.put("returningCustomersRate", Math.round(returningCustomersRate * 100.0) / 100.0);
        insights.put("customerLifetimeValue", customerLifetimeValue);
        insights.put("averageRetentionDays", averageRetentionDays);
        insights.put("topSellingProduct", topSellingProduct);
        insights.put("topCategory", topCategory);
        insights.put("inventoryTurnover", Math.round(inventoryTurnover * 100.0) / 100.0);
        insights.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        insights.put("todayOrders", todayOrders);
        insights.put("todayCustomers", todayCustomers);

        return insights;
    }

    public List<Map<String, Object>> getHourlySales(String dateStr) {
        LocalDate date = dateStr != null
                ? LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
                : LocalDate.now();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get all COMPLETED orders for the selected date
        long totalOrders = donHangRepository.countByTrangThaiAndDatLucBetween(
                OrderStatus.HOAN_TAT, startOfDay, endOfDay);

        BigDecimal totalRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThai(
                OrderStatus.HOAN_TAT, startOfDay, endOfDay);

        // Count unique customers (simplified - just use order count as proxy)
        long totalCustomers = totalOrders; // TODO: Count distinct customers

        // Initialize 24 hours with 0 values
        List<Map<String, Object>> hourlySales = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", hour);
            hourData.put("time", String.format("%02d:00", hour));

            // For now, distribute evenly across hours (simplified)
            // TODO: Query actual hourly data from DB
            hourData.put("orders", totalOrders > 0 ? (totalOrders / 24) : 0);
            hourData.put("revenue",
                    totalRevenue != null
                            ? totalRevenue.divide(BigDecimal.valueOf(24), 0, RoundingMode.HALF_UP).longValue()
                            : 0);
            hourData.put("customers", totalCustomers > 0 ? (totalCustomers / 24) : 0);

            hourlySales.add(hourData);
        }

        return hourlySales;
    }

    public Map<String, Object> getPerformanceMetrics(String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseDate(startDateStr, LocalDate.now().minusDays(30));
        LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        long totalOrders = donHangRepository.countByDatLucBetween(startDate, endDate);
        long completedOrders = donHangRepository.countByTrangThaiAndDatLucBetween(OrderStatus.HOAN_TAT, startDate,
                endDate);
        long cancelledOrders = donHangRepository.countByTrangThaiAndDatLucBetween(OrderStatus.DA_HUY, startDate,
                endDate);
        BigDecimal totalRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThai(OrderStatus.HOAN_TAT,
                startDate, endDate);

        // Count carts created in the period
        long totalCarts = gioHangRepository.countByTaoLucBetween(startDate, endDate);

        // Calculate conversion rate: orders / carts * 100
        // Cap at 100% if orders exceed carts (some orders may be created directly
        // without cart)
        double conversionRate = totalCarts > 0
                ? Math.min(100.0, (double) totalOrders / totalCarts * 100)
                : (totalOrders > 0 ? 100.0 : 0.0);

        // Calculate cart abandonment rate: (carts - orders) / carts * 100
        // Ensure it's between 0 and 100%
        double cartAbandonmentRate = 0.0;
        if (totalCarts > 0) {
            if (totalOrders <= totalCarts) {
                cartAbandonmentRate = (double) (totalCarts - totalOrders) / totalCarts * 100;
            } else {
                // If orders > carts, it means some orders were created directly
                // In this case, abandonment rate is 0 (all carts converted)
                cartAbandonmentRate = 0.0;
            }
        } else if (totalOrders > 0) {
            // No carts but have orders - orders created directly, no abandonment
            cartAbandonmentRate = 0.0;
        }

        // Calculate completion rate
        double completionRate = totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0.0;

        // Calculate cancellation rate
        double cancellationRate = totalOrders > 0 ? (double) cancelledOrders / totalOrders * 100 : 0.0;

        // Customer satisfaction: Use completion rate as proxy (completed orders
        // indicate satisfaction)
        // In a real system, this would come from reviews/ratings
        double customerSatisfaction = completionRate; // Simplified: assume satisfied if order completed

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalOrders", totalOrders);
        metrics.put("completedOrders", completedOrders);
        metrics.put("cancelledOrders", cancelledOrders);
        metrics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        metrics.put("cancellationRate", Math.round(cancellationRate * 100.0) / 100.0);
        // Add new metrics
        metrics.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
        metrics.put("cartAbandonmentRate", Math.round(cartAbandonmentRate * 100.0) / 100.0);
        metrics.put("customerSatisfaction", Math.round(customerSatisfaction * 100.0) / 100.0);

        return metrics;
    }

    public Map<String, Object> getOrderDistribution(String type, String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseDate(startDateStr, LocalDate.now().minusDays(30));
        LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        Map<String, Object> distribution = new HashMap<>();

        if ("status".equals(type)) {
            distribution = getOrderStatusCounts();
        } else {
            long totalOrders = donHangRepository.countByDatLucBetween(startDate, endDate);
            distribution.put("total", totalOrders);
        }

        return distribution;
    }

    public Map<String, Object> getCustomerAnalytics(String type, String startDateStr, String endDateStr) {
        // Note: startDate and endDate are parsed but not used in current implementation
        // They are reserved for future filtering by date range
        // LocalDateTime startDate = parseDate(startDateStr,
        // LocalDate.now().minusDays(30));
        // LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        Map<String, Object> analytics = new HashMap<>();

        if ("segments".equals(type)) {
            long totalCustomers = khachHangRepository.count();
            long activeCustomers = donHangRepository.countCustomersWithCompletedOrders(OrderStatus.HOAN_TAT);
            long repeatCustomers = donHangRepository.countRepeatCustomers(OrderStatus.HOAN_TAT);

            // Calculate new customers (customers who haven't made any orders yet)
            long newCustomers = totalCustomers - activeCustomers;

            // Calculate VIP customers (customers with 3+ completed orders)
            // For now, use repeat customers as proxy for VIP (customers with multiple
            // orders)
            long vipCustomers = repeatCustomers; // TODO: Implement proper VIP calculation based on order value/quantity

            analytics.put("total", totalCustomers);
            analytics.put("active", activeCustomers);
            analytics.put("repeat", repeatCustomers);
            // Add fields that frontend expects
            analytics.put("newCustomers", newCustomers);
            analytics.put("returningCustomers", repeatCustomers);
            analytics.put("vipCustomers", vipCustomers);
        } else {
            long totalCustomers = khachHangRepository.count();
            analytics.put("total", totalCustomers);
        }

        return analytics;
    }

    // Helper methods
    private double calculateGrowth(long current, long previous) {
        if (previous == 0)
            return current > 0 ? 100.0 : 0.0;
        return ((double) (current - previous) / previous) * 100;
    }

    private double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        if (current == null)
            current = BigDecimal.ZERO;

        BigDecimal diff = current.subtract(previous);
        BigDecimal growth = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return growth.doubleValue();
    }

    private LocalDateTime parseDate(String dateStr, LocalDate defaultDate) {
        if (dateStr == null || dateStr.isEmpty()) {
            return defaultDate.atStartOfDay();
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        } catch (Exception e) {
            return defaultDate.atStartOfDay();
        }
    }
}
