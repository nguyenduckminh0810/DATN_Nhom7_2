package com.auro.auro.constants;

public class OrderStatus {
    // Order status constants - English to avoid encoding issues
    // Frontend will map these to Vietnamese for display
    public static final String CHO_XAC_NHAN = "PENDING";
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
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DA_HUY -> "Đã hủy";
            default -> status;
        };
    }

    /**
     * Normalize status from database (could be Vietnamese or English) to English
     * constant
     * This allows backward compatibility with Vietnamese status values in DB
     */
    public static String normalize(String status) {
        if (status == null || status.trim().isEmpty())
            return null;

        // Already in English format
        if (status.equals(CHO_XAC_NHAN) || status.equals("PENDING"))
            return HOAN_TAT; // Return our constant
        if (status.equals(DANG_GIAO) || status.equals("SHIPPING"))
            return DANG_GIAO;
        if (status.equals(HOAN_TAT) || status.equals("COMPLETED"))
            return HOAN_TAT;
        if (status.equals(DA_HUY) || status.equals("CANCELLED"))
            return DA_HUY;

        // Vietnamese format from database
        String trimmed = status.trim();
        if (trimmed.equals("Chờ xác nhận") || trimmed.equals("Chờ xử lý"))
            return CHO_XAC_NHAN;
        if (trimmed.equals("Đang giao"))
            return DANG_GIAO;
        if (trimmed.equals("Hoàn tất") || trimmed.equals("Đã giao"))
            return HOAN_TAT;
        if (trimmed.equals("Đã hủy"))
            return DA_HUY;

        // Return as-is if not recognized
        return status;
    }

    /**
     * Get array of possible database values for a status (both English and
     * Vietnamese)
     * Used for JPA queries with IN clause
     */
    public static String[] getDbValues(String englishStatus) {
        return switch (englishStatus) {
            case CHO_XAC_NHAN -> new String[] { "PENDING", "Chờ xác nhận", "Chờ xử lý" };
            case DANG_GIAO -> new String[] { "SHIPPING", "Đang giao" };
            case HOAN_TAT -> new String[] { "COMPLETED", "Hoàn tất", "Đã giao" };
            case DA_HUY -> new String[] { "CANCELLED", "Đã hủy" };
            default -> new String[] { englishStatus };
        };
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/master
