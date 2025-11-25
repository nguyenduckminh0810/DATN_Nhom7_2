-- ============================================
-- CHUẨN HÓA TRẠNG THÁI ĐƠN HÀNG VỀ TIẾNG ANH
-- Chạy 1 lần duy nhất để fix dữ liệu không đồng nhất
-- Backend: Tiếng Anh (tránh encoding issues)
-- Frontend: Tiếng Việt (mapping để hiển thị)
-- ============================================

-- BACKUP trước khi chạy (QUAN TRỌNG!)
SELECT * INTO don_hang_backup_before_normalize
FROM don_hang;

-- 1. Chuẩn hóa về "COMPLETED"
UPDATE don_hang 
SET trang_thai = 'COMPLETED' 
WHERE trang_thai IN (
    N'Hoàn tất', N'Hoàn Tất', N'HOÀN TẤT', N'hoàn tất',
    N'Đã giao', N'Đã Giao', N'ĐÃ GIAO', N'đã giao',
    'Hoan tat', 'Da giao'
);

-- 2. Chuẩn hóa về "SHIPPING"
UPDATE don_hang 
SET trang_thai = 'SHIPPING' 
WHERE trang_thai IN (
    N'Đang giao', N'Đang Giao', N'ĐANG GIAO', N'đang giao',
    'Dang giao'
);

-- 3. Chuẩn hóa về "CONFIRMED"
UPDATE don_hang 
SET trang_thai = 'CONFIRMED' 
WHERE trang_thai IN (
    N'Đã xác nhận', N'Đã Xác Nhận', N'ĐÃ XÁC NHẬN', N'đã xác nhận',
    'Da xac nhan'
);

-- 4. Chuẩn hóa về "PENDING"
UPDATE don_hang 
SET trang_thai = 'PENDING' 
WHERE trang_thai IN (
    N'Chờ xác nhận', N'Chờ Xác Nhận', N'CHỜ XÁC NHẬN', N'chờ xác nhận',
    'Cho xac nhan'
);

-- 5. Chuẩn hóa về "CANCELLED"
UPDATE don_hang 
SET trang_thai = 'CANCELLED' 
WHERE trang_thai IN (
    N'Đã hủy', N'Đã Hủy', N'ĐÃ HỦY', N'đã hủy',
    'Da huy'
);

-- ============================================
-- KIỂM TRA KẾT QUẢ
-- ============================================

-- Xem phân bố trạng thái sau khi chuẩn hóa
SELECT trang_thai, COUNT(*) as so_luong
FROM don_hang
GROUP BY trang_thai
ORDER BY trang_thai;

-- Kết quả mong đợi (chỉ 5 giá trị tiếng Anh):
-- CANCELLED
-- COMPLETED
-- CONFIRMED
-- PENDING
-- SHIPPING

-- Kiểm tra có trạng thái nào lạ không
SELECT DISTINCT trang_thai
FROM don_hang
WHERE trang_thai NOT IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED');

-- Nếu có kết quả → Cần xử lý thủ công

-- ============================================
-- ROLLBACK (nếu cần)
-- ============================================
-- Chỉ chạy nếu muốn khôi phục dữ liệu cũ
-- DELETE FROM don_hang;
-- INSERT INTO don_hang SELECT * FROM don_hang_backup_before_normalize;
-- DROP TABLE don_hang_backup_before_normalize;

-- ============================================
-- LƯU Ý
-- ============================================
-- 1. Sau khi chạy SQL này, RESTART Spring Boot app
-- 2. Frontend sẽ tự động map sang tiếng Việt để hiển thị
-- 3. Từ giờ trở đi, code chỉ dùng constants tiếng Anh
-- 4. Không bao giờ hardcode trạng thái nữa
-- ============================================
