-- Kiểm tra tên cột trong bảng khach_hang
DESCRIBE auro1.khach_hang;

-- hoặc
SHOW COLUMNS FROM auro1.khach_hang;

-- Xem data thực tế
SELECT * FROM auro1.khach_hang WHERE id = 8;
