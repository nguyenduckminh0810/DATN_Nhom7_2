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
    private final HinhAnhRepository hinhAnhRepository;
    private final DanhGiaSanPhamRepository danhGiaSanPhamRepository;

    /**
     * Helper method to get list of possible status values in DB
     * Supports both Vietnamese and English status values
     */
    private List<String> getStatusValues(String englishStatus) {
        return Arrays.asList(OrderStatus.getDbValues(englishStatus));
    }

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

        // Doanh thu hôm nay - tính theo datLuc (ngày đặt hàng) của đơn hàng đã hoàn tất
        // Support both Vietnamese "Hoàn tất" and English "COMPLETED"
        List<String> completedStatusValues = getStatusValues(OrderStatus.HOAN_TAT);
        BigDecimal revenueToday = donHangRepository.sumRevenueByDatLucBetweenAndTrangThaiIn(completedStatusValues,
                startOfToday, now);
        // Nếu không có doanh thu theo datLuc, thử tính theo taoLuc
        if (revenueToday == null || revenueToday.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal revenueByTaoLuc = donHangRepository.sumRevenueByTaoLucBetween(startOfToday, now);
            if (revenueByTaoLuc != null && revenueByTaoLuc.compareTo(BigDecimal.ZERO) > 0) {
                revenueToday = revenueByTaoLuc;
            }
        }

        // Yesterday stats for comparison
        long ordersYesterday = donHangRepository.countByTaoLucBetween(startOfYesterday, endOfYesterday);
        BigDecimal revenueYesterday = donHangRepository.sumRevenueByDatLucBetweenAndTrangThaiIn(completedStatusValues,
                startOfYesterday, endOfYesterday);

        // Calculate growth
        double orderGrowth = calculateGrowth(ordersToday, ordersYesterday);
        double revenueGrowth = calculateGrowth(revenueToday, revenueYesterday);

        // Customer stats - Đếm từ bảng khach_hang
        // Ưu tiên dùng khachHang.taoLuc nếu có, nếu không thì dùng taiKhoan.taoLuc
        // Điều này bao gồm cả trường hợp admin tạo KhachHang từ TaiKhoan đã tồn tại
        long totalCustomers = khachHangRepository.count();
        long newCustomersToday = khachHangRepository.countNewCustomersBetween(startOfToday, now);
        long newCustomersYesterday = khachHangRepository.countNewCustomersBetween(startOfYesterday, endOfYesterday);
        double customersGrowth = calculateGrowth(newCustomersToday, newCustomersYesterday);

        // Debug logging
        System.out.println("👥 [CUSTOMER DEBUG] Querying from: khach_hang table");
        System.out.println("👥 [CUSTOMER DEBUG] Start of Today: " + startOfToday);
        System.out.println("👥 [CUSTOMER DEBUG] Now: " + now);
        System.out.println("👥 [CUSTOMER DEBUG] New Customers Today: " + newCustomersToday);
        System.out.println("👥 [CUSTOMER DEBUG] Total Customers: " + totalCustomers);

        // Low stock count - Đếm số SẢN PHẨM (không phải biến thể) có ít nhất 1 biến thể
        // sắp hết hoặc hết hàng
        // Bao gồm cả biến thể hết hàng (stock = 0) và sắp hết (0 < stock <= threshold)
        long lowStockCount = bienTheSanPhamRepository.countProductsWithLowStock(lowStockThreshold);

        // Debug: Kiểm tra tổng số biến thể và các trường hợp khác
        long totalVariants = bienTheSanPhamRepository.count();
        long lowStockVariants = bienTheSanPhamRepository.countLowStockVariants(lowStockThreshold);
        long allLowStockVariants = bienTheSanPhamRepository.countBySoLuongTonLessThanEqual(lowStockThreshold);

        // Debug: Xem chi tiết stock của tất cả biến thể và sản phẩm
        try {
            List<Object[]> allVariants = bienTheSanPhamRepository.findAllWithStock();
            List<Long> productIdsWithLowStock = bienTheSanPhamRepository.findProductIdsWithLowStock(lowStockThreshold);

            System.out.println("📦 [LOW STOCK DEBUG] Threshold: " + lowStockThreshold);
            System.out.println("📦 [LOW STOCK DEBUG] Products with Low Stock (distinct products): " + lowStockCount);
            System.out.println("📦 [LOW STOCK DEBUG] Product IDs with Low Stock: " + productIdsWithLowStock);
            System.out.println("📦 [LOW STOCK DEBUG] Low Stock Variants (>0 and <=" + lowStockThreshold + "): "
                    + lowStockVariants);
            System.out.println("📦 [LOW STOCK DEBUG] All Low Stock Variants (<= " + lowStockThreshold
                    + ", including 0): " + allLowStockVariants);
            System.out.println("📦 [LOW STOCK DEBUG] Total Variants: " + totalVariants);

            // Đếm số sản phẩm có biến thể hết hàng (stock = 0)
            long productsOutOfStock = bienTheSanPhamRepository.countProductsWithLowStock(0);
            System.out.println(
                    "📦 [LOW STOCK DEBUG] Products with Out of Stock variants (stock = 0): " + productsOutOfStock);

            System.out.println("📦 [LOW STOCK DEBUG] All Variants Stock Details:");
            for (Object[] variant : allVariants) {
                Long id = (Long) variant[0];
                Integer stock = (Integer) variant[1];
                System.out.println("  - Variant ID: " + id + ", Stock: " + (stock != null ? stock : "NULL"));
            }
        } catch (Exception e) {
            System.out.println("📦 [LOW STOCK DEBUG] Error getting variant details: " + e.getMessage());
            e.printStackTrace();
        }

        Map<String, Object> summary = new HashMap<>();
        // Convert BigDecimal to number for JSON serialization
        summary.put("revenueToday", revenueToday != null ? revenueToday.doubleValue() : 0.0);
        summary.put("revenueGrowth", Math.round(revenueGrowth * 100.0) / 100.0);
        summary.put("newOrders24h", orders24h);
        summary.put("ordersGrowth", Math.round(orderGrowth * 100.0) / 100.0);
        summary.put("newCustomersToday", newCustomersToday);
        summary.put("customersGrowth", Math.round(customersGrowth * 100.0) / 100.0);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalCustomers", totalCustomers);

        // Debug logging
        System.out.println("📊 [THONG KE SUMMARY]");
        System.out.println("  - Revenue Today: " + summary.get("revenueToday"));
        System.out.println("  - Low Stock Count: " + summary.get("lowStockCount") + " (type: " + lowStockCount + ")");
        System.out.println("  - New Customers Today: " + summary.get("newCustomersToday"));
        System.out.println("  - Summary map keys: " + summary.keySet());
        System.out.println("  - Summary map values: " + summary);

        return summary;
    }

    public Map<String, Object> getAlerts(Integer lowStockThreshold) {
        if (lowStockThreshold == null)
            lowStockThreshold = 10;

        System.out.println("🚨 [ALERTS DEBUG]");
        System.out.println("  - Low Stock Threshold: " + lowStockThreshold);

        // Low stock count - Đếm số SẢN PHẨM (không phải biến thể) có ít nhất 1 biến thể
        // sắp hết hoặc hết hàng
        // Bao gồm cả biến thể hết hàng (stock = 0) và sắp hết (0 < stock <= threshold)
        long lowStockCount = bienTheSanPhamRepository.countProductsWithLowStock(lowStockThreshold);
        System.out.println("  - Low Stock Products: " + lowStockCount);

        // Đếm đơn hàng chờ xác nhận - hỗ trợ cả tiếng Anh và tiếng Việt
        long pendingOrders = donHangRepository.countByTrangThai(OrderStatus.CHO_XAC_NHAN) +
                donHangRepository.countByTrangThai("Chờ xác nhận") +
                donHangRepository.countByTrangThai("CHO_XAC_NHAN");
        System.out.println("  - Pending Orders (status=" + OrderStatus.CHO_XAC_NHAN + "): " + pendingOrders);

        // Đếm đơn hàng đang giao - hỗ trợ cả tiếng Anh và tiếng Việt
        long shippingOrders = donHangRepository.countByTrangThai(OrderStatus.DANG_GIAO) +
                donHangRepository.countByTrangThai("Đang giao") +
                donHangRepository.countByTrangThai("DANG_GIAO");
        System.out.println("  - Shipping Orders (status=" + OrderStatus.DANG_GIAO + "): " + shippingOrders);

        Map<String, Object> alerts = new HashMap<>();
        alerts.put("lowStockProducts", lowStockCount);
        alerts.put("pendingOrders", pendingOrders);
        alerts.put("needShipping", shippingOrders);

        System.out.println("  - Final Alerts: " + alerts);
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

        System.out.println("📊 [CHART DEBUG]");
        System.out.println("  - Range: " + range + " (" + days + " days)");
        System.out.println("  - Metric: " + metric);
        System.out.println("  - Current Period: " + currentStart + " to " + now);
        System.out.println("  - Previous Period: " + previousStart + " to " + previousEnd);

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
            System.out.println("  - Current Revenue Records: " + currentRevenue.size());
            for (Object[] row : currentRevenue) {
                currentDataMap.put(row[0].toString(), (Number) row[1]);
            }

            // Previous period revenue
            List<Object[]> previousRevenue = donHangRepository.sumRevenueByDateBetween(
                    OrderStatus.HOAN_TAT, previousStart, previousEnd);
            System.out.println("  - Previous Revenue Records: " + previousRevenue.size());
            for (Object[] row : previousRevenue) {
                // Shift dates forward by 'days' to align with current period
                LocalDate oldDate = LocalDate.parse(row[0].toString());
                LocalDate newDate = oldDate.plusDays(days);
                previousDataMap.put(newDate.toString(), (Number) row[1]);
            }
        } else if ("orders".equals(metric)) {
            // Current period orders
            List<Object[]> currentOrders = donHangRepository.countOrdersByDateBetween(currentStart, now);
            System.out.println("  - Current Orders Records: " + currentOrders.size());
            for (Object[] row : currentOrders) {
                currentDataMap.put(row[0].toString(), (Number) row[1]);
            }

            // Previous period orders
            List<Object[]> previousOrders = donHangRepository.countOrdersByDateBetween(previousStart, previousEnd);
            System.out.println("  - Previous Orders Records: " + previousOrders.size());
            for (Object[] row : previousOrders) {
                LocalDate oldDate = LocalDate.parse(row[0].toString());
                LocalDate newDate = oldDate.plusDays(days);
                previousDataMap.put(newDate.toString(), (Number) row[1]);
            }
        } else if ("customers".equals(metric)) {
            // Current period customers (distinct customers who placed orders)
            List<Object[]> currentCustomers = donHangRepository.countCustomersByDateBetween(currentStart, now);
            System.out.println("  - Current Customers Records: " + currentCustomers.size());
            for (Object[] row : currentCustomers) {
                currentDataMap.put(row[0].toString(), (Number) row[1]);
            }

            // Previous period customers
            List<Object[]> previousCustomers = donHangRepository.countCustomersByDateBetween(previousStart, previousEnd);
            System.out.println("  - Previous Customers Records: " + previousCustomers.size());
            for (Object[] row : previousCustomers) {
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

        System.out.println("  - Labels count: " + labels.size());
        System.out.println("  - Current data count: " + currentData.size());
        System.out.println("  - Previous data count: " + previousData.size());

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

        System.out.println("📦 [TOP PRODUCTS DEBUG]");
        System.out.println("  - Limit: " + limit);
        System.out.println("  - Range Days: " + rangeDays);
        System.out.println("  - Start Date: " + startDate);
        System.out.println("  - End Date: " + now);
        System.out.println("  - OrderStatus.HOAN_TAT: " + OrderStatus.HOAN_TAT);

        List<Object[]> topProducts = donHangChiTietRepository.findTopProductsBetween(OrderStatus.HOAN_TAT, startDate,
                now, pageable);

        System.out.println("  - Found " + topProducts.size() + " top products");

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : topProducts) {
            Long productId = ((Number) row[0]).longValue();
            String productName = (String) row[1];
            Long sales = ((Number) row[2]).longValue();
            BigDecimal revenue = (BigDecimal) row[3];

            System.out.println("  - Product ID: " + productId + ", Name: " + productName + ", Sales: " + sales
                    + ", Revenue: " + revenue);

            Map<String, Object> product = new HashMap<>();
            product.put("id", productId);
            product.put("name", productName);
            product.put("sales", sales);
            product.put("revenue", revenue != null ? revenue.doubleValue() : 0.0);

            // Lấy ảnh đại diện của sản phẩm
            String imageUrl = null;
            try {
                var images = hinhAnhRepository.findBySanPham_IdOrderByThuTuAscIdAsc(productId);
                if (images != null && !images.isEmpty()) {
                    imageUrl = images.stream()
                            .filter(ha -> Boolean.TRUE.equals(ha.getLaDaiDien()))
                            .map(com.auro.auro.model.HinhAnh::getUrl)
                            .findFirst()
                            .orElseGet(() -> images.get(0).getUrl());
                    System.out.println("  - Image URL: " + imageUrl);
                } else {
                    System.out.println("  - No images found for product " + productId);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error loading image for product " + productId + ": " + e.getMessage());
                e.printStackTrace();
            }
            product.put("image", imageUrl);

            result.add(product);
        }

        System.out.println("  - Returning " + result.size() + " products");
        return result;
    }

    public List<Map<String, Object>> getRecentOrders(Integer limit) {
        if (limit == null)
            limit = 10;

        System.out.println("📋 [RECENT ORDERS DEBUG]");
        System.out.println("  - Limit: " + limit);

        Pageable pageable = PageRequest.of(0, limit);
        var orders = donHangRepository.findRecentOrders(pageable);

        System.out.println("  - Found " + orders.size() + " recent orders");

        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderCode", order.getSoDonHang());
            orderMap.put("customer", order.getKhachHang() != null ? order.getKhachHang().getHoTen() : "Khách vãng lai");
            orderMap.put("amount", order.getTongThanhToan() != null ? order.getTongThanhToan().doubleValue() : 0.0);
            orderMap.put("status", order.getTrangThai());
            orderMap.put("time", order.getDatLuc() != null ? order.getDatLuc().toString() : "");

            // Add payment method
            orderMap.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");

            // Add payment status
            orderMap.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus() : "pending");

            // Add product count from order details
            int productCount = order.getChiTietList() != null ? order.getChiTietList().size() : 0;
            orderMap.put("productCount", productCount);

            // Extract city from shipping address
            String city = "";
            if (order.getDiaChiGiao() != null && !order.getDiaChiGiao().isEmpty()) {
                String[] parts = order.getDiaChiGiao().split(",");
                if (parts.length > 0) {
                    city = parts[parts.length - 1].trim(); // Last part is usually the city/province
                }
            }
            orderMap.put("shippingCity", city);

            System.out.println("  - Order #" + order.getSoDonHang() + ", Customer: " + orderMap.get("customer")
                    + ", Amount: " + orderMap.get("amount") + ", Products: " + productCount);
            return orderMap;
        }).collect(Collectors.toList());

        System.out.println("  - Returning " + result.size() + " orders");
        return result;
    }

    public Map<String, Object> getCustomerSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

        long totalCustomers = khachHangRepository.count();
        long customersWithOrders = donHangRepository.countCustomersWithCompletedOrders(OrderStatus.HOAN_TAT);
        long repeatCustomers = donHangRepository.countRepeatCustomers(OrderStatus.HOAN_TAT);
        long newCustomersToday = khachHangRepository.countNewCustomersBetween(startOfToday, now);

        double repeatRate = totalCustomers > 0
                ? (double) repeatCustomers / totalCustomers * 100
                : 0.0;

        // Tính average rating từ tất cả đánh giá
        double averageRating = 0.0;
        try {
            // Lấy tất cả đánh giá và tính trung bình
            var allRatings = danhGiaSanPhamRepository.findAll();
            if (allRatings != null && !allRatings.isEmpty()) {
                double totalStars = allRatings.stream()
                        .filter(dg -> dg.getSoSao() != null)
                        .mapToInt(com.auro.auro.model.DanhGiaSanPham::getSoSao)
                        .sum();
                int count = (int) allRatings.stream()
                        .filter(dg -> dg.getSoSao() != null)
                        .count();
                if (count > 0) {
                    averageRating = Math.round((totalStars / count) * 10.0) / 10.0;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error calculating average rating: " + e.getMessage());
        }

        System.out.println("👥 [CUSTOMER SUMMARY DEBUG]");
        System.out.println("  - Total Customers: " + totalCustomers);
        System.out.println("  - Customers With Orders: " + customersWithOrders);
        System.out.println("  - Repeat Customers: " + repeatCustomers);
        System.out.println("  - New Customers Today: " + newCustomersToday);
        System.out.println("  - Repeat Rate: " + repeatRate + "%");
        System.out.println("  - Average Rating: " + averageRating);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", totalCustomers);
        summary.put("customersWithOrders", customersWithOrders);
        summary.put("repeatCustomers", repeatCustomers);
        summary.put("repeatRate", Math.round(repeatRate * 100.0) / 100.0);
        summary.put("newCustomersToday", newCustomersToday);
        summary.put("averageRating", averageRating);

        return summary;
    }

    public List<Map<String, Object>> getCategoryPerformance(Integer limit, Integer rangeDays) {
        if (limit == null)
            limit = 5;
        if (rangeDays == null)
            rangeDays = 30;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(rangeDays);
        LocalDateTime previousStart = startDate.minusDays(rangeDays);
        LocalDateTime previousEnd = startDate.minusSeconds(1);
        Pageable pageable = PageRequest.of(0, limit);

        System.out.println("📊 [CATEGORY PERFORMANCE DEBUG]");
        System.out.println("  - Limit: " + limit);
        System.out.println("  - Range Days: " + rangeDays);
        System.out.println("  - Current Period: " + startDate + " to " + now);
        System.out.println("  - Previous Period: " + previousStart + " to " + previousEnd);

        // Current period
        List<Object[]> categoryData = donHangChiTietRepository.findCategoryPerformanceBetween(OrderStatus.HOAN_TAT,
                startDate, now, pageable);

        // Previous period for comparison
        List<Object[]> previousCategoryData = donHangChiTietRepository.findCategoryPerformanceBetween(
                OrderStatus.HOAN_TAT,
                previousStart, previousEnd, pageable);

        // Build map of previous period revenue by category ID
        Map<Long, BigDecimal> previousRevenueMap = new HashMap<>();
        for (Object[] row : previousCategoryData) {
            Long categoryId = ((Number) row[0]).longValue();
            BigDecimal revenue = (BigDecimal) row[3];
            previousRevenueMap.put(categoryId, revenue);
        }

        System.out.println("  - Found " + categoryData.size() + " categories in current period");
        System.out.println("  - Found " + previousCategoryData.size() + " categories in previous period");

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : categoryData) {
            Long categoryId = ((Number) row[0]).longValue();
            String categoryName = (String) row[1];
            Long sales = ((Number) row[2]).longValue();
            BigDecimal revenue = (BigDecimal) row[3];

            // Calculate change vs previous period
            BigDecimal previousRevenue = previousRevenueMap.getOrDefault(categoryId, BigDecimal.ZERO);
            double change = 0.0;
            String trend = "neutral";
            String trendIcon = "bi bi-dash";

            if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                double changePercent = ((revenue.doubleValue() - previousRevenue.doubleValue())
                        / previousRevenue.doubleValue()) * 100;
                change = Math.round(changePercent * 10.0) / 10.0;

                if (change > 0) {
                    trend = "up";
                    trendIcon = "bi bi-graph-up-arrow";
                } else if (change < 0) {
                    trend = "down";
                    trendIcon = "bi bi-graph-down-arrow";
                }
            } else if (revenue.compareTo(BigDecimal.ZERO) > 0) {
                // New category with sales
                change = 100.0;
                trend = "up";
                trendIcon = "bi bi-graph-up-arrow";
            }

            System.out.println("  - Category: " + categoryName + ", Sales: " + sales + ", Revenue: " + revenue
                    + ", Change: " + change + "%");

            Map<String, Object> category = new HashMap<>();
            category.put("id", categoryId);
            category.put("name", categoryName);
            category.put("sales", sales);
            category.put("revenue", revenue != null ? revenue.doubleValue() : 0.0);
            category.put("change", change);
            category.put("trend", trend);
            category.put("trendIcon", trendIcon);
            result.add(category);
        }

        System.out.println("  - Returning " + result.size() + " categories");
        return result;
    }

    public Map<String, Object> getOrderStatusCounts() {
        Map<String, Object> statusCounts = new HashMap<>();
        // Support both Vietnamese and English status values
        statusCounts.put("pending", donHangRepository.countByTrangThaiIn(getStatusValues(OrderStatus.CHO_XAC_NHAN)));
        statusCounts.put("shipping", donHangRepository.countByTrangThaiIn(getStatusValues(OrderStatus.DANG_GIAO)));
        statusCounts.put("completed", donHangRepository.countByTrangThaiIn(getStatusValues(OrderStatus.HOAN_TAT)));
        statusCounts.put("cancelled", donHangRepository.countByTrangThaiIn(getStatusValues(OrderStatus.DA_HUY)));

        return statusCounts;
    }

    public Map<String, Object> getAnalyticsKpis(String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseDate(startDateStr, LocalDate.now().minusDays(30));
        LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        System.out.println("📊 [ANALYTICS KPIS DEBUG]");
        System.out.println("  - Start Date: " + startDate);
        System.out.println("  - End Date: " + endDate);
        System.out.println("  - OrderStatus.HOAN_TAT: " + OrderStatus.HOAN_TAT);

        // Calculate period length in days
        long periodDays = java.time.Duration.between(startDate, endDate).toDays();
        if (periodDays == 0)
            periodDays = 1;

        // Previous period for comparison
        LocalDateTime previousStart = startDate.minusDays(periodDays);
        LocalDateTime previousEnd = startDate.minusSeconds(1);

        // Current period stats
        List<String> completedStatusValues = getStatusValues(OrderStatus.HOAN_TAT);
        long totalOrders = donHangRepository.countByDatLucBetween(startDate, endDate);
        long completedOrders = donHangRepository.countByTrangThaiInAndDatLucBetween(completedStatusValues, startDate,
                endDate);
        BigDecimal totalRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThaiIn(completedStatusValues,
                startDate, endDate);
        long totalCustomers = khachHangRepository.count();
        long totalProducts = sanPhamRepository.count();

        System.out.println("  - Total Orders (all status): " + totalOrders);
        System.out.println("  - Completed Orders: " + completedOrders);
        System.out.println("  - Total Revenue: " + (totalRevenue != null ? totalRevenue : "NULL"));

        // Previous period stats for growth calculation
        long previousOrders = donHangRepository.countByDatLucBetween(previousStart, previousEnd);
        BigDecimal previousRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThaiIn(completedStatusValues,
                previousStart, previousEnd);
        
        System.out.println("  - Previous Orders: " + previousOrders);
        System.out.println("  - Previous Revenue: " + (previousRevenue != null ? previousRevenue : "NULL"));

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

        // Debug logging
        System.out.println("📈 [ANALYTICS KPIs DEBUG]");
        System.out.println("📈 Date range: " + startDate + " to " + endDate);
        System.out.println("📈 Total Revenue: " + totalRevenue);
        System.out.println("📈 Total Orders: " + totalOrders);
        System.out.println("📈 Total Customers: " + totalCustomers);
        System.out.println("📈 Total Products: " + totalProducts);
        System.out.println("📈 Revenue Growth: " + revenueGrowth + "%");

        return kpis;
    }

    public Map<String, Object> getBusinessInsights(String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseDate(startDateStr, LocalDate.now().minusDays(30));
        LocalDateTime endDate = parseDate(endDateStr, LocalDate.now());

        List<String> completedStatusValues = getStatusValues(OrderStatus.HOAN_TAT);
        long newCustomers = khachHangRepository.count();
        long repeatCustomers = donHangRepository.countRepeatCustomersIn(completedStatusValues);
        long totalOrders = donHangRepository.countByDatLucBetween(startDate, endDate);
        BigDecimal totalRevenue = donHangRepository.sumRevenueByDatLucBetweenAndTrangThaiIn(completedStatusValues,
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
            long totalSoldQuantity = donHangChiTietRepository.findTotalSoldQuantityBetweenIn(
                    completedStatusValues, startDate, endDate);
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

        // Calculate products sold (total quantity sold in period)
        long productsSold = donHangChiTietRepository.findTotalSoldQuantityBetweenIn(
                completedStatusValues, startDate, endDate);
        insights.put("productsSold", productsSold);

        // Debug logging
        System.out.println("📊 [BUSINESS INSIGHTS DEBUG]");
        System.out.println("📊 Date range: " + startDate + " to " + endDate);
        System.out.println("📊 Today's Revenue: " + todayRevenue);
        System.out.println("📊 Today's Orders: " + todayOrders);
        System.out.println("📊 Today's Customers: " + todayCustomers);
        System.out.println("📊 Products Sold (period): " + productsSold);
        System.out.println("📊 Total Orders (period): " + totalOrders);
        System.out.println("📊 Total Revenue (period): " + totalRevenue);

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