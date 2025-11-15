package com.auro.auro.constants;

public class OrderStatus {
    // Order status constants - English to avoid encoding issues
    // Frontend will map these to Vietnamese for display
    public static final String CHO_XAC_NHAN = "PENDING";
    public static final String DA_XAC_NHAN = "CONFIRMED";
    public static final String DANG_GIAO = "SHIPPING";
    public static final String HOAN_TAT = "COMPLETED";
    public static final String DA_HUY = "CANCELLED";

    private OrderStatus() {
        // Prevent instantiation
    }

    /**
     * Get Vietnamese display name for status
     * Use this method when you need to display status in Vietnamese
     */
    public static String getDisplayName(String status) {
        if (status == null)
            return "";

        return switch (status) {
            case HOAN_TAT -> "Hoàn tất";
            case DANG_GIAO -> "Đang giao";
            case DA_XAC_NHAN -> "Đã xác nhận";
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DA_HUY -> "Đã hủy";
            default -> status;
        };
    }
}
