-- Kiểm tra schema thực tế trong DB
-- Chạy câu lệnh này để xem cấu trúc bảng khach_hang
EXEC sp_help 'dbo.khach_hang';

-- Hoặc xem cột cụ thể
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'khach_hang'
ORDER BY ORDINAL_POSITION;

-- Kiểm tra xem user khach1@gmail.com có bản ghi trong khach_hang không
SELECT 
    tk.id as tai_khoan_id,
    tk.email,
    tk.ho_ten,
    kh.id as khach_hang_id,
    kh.ho_ten as ten_khach_hang
FROM tai_khoan tk
LEFT JOIN khach_hang kh ON tk.id = kh.id_tai_khoan
WHERE tk.email = 'khach1@gmail.com';

-- Xem tất cả roles của user này
SELECT 
    tk.email,
    vt.ma_vai_tro,
    vt.ten_vai_tro
FROM tai_khoan tk
LEFT JOIN tai_khoan_vai_tro tkvt ON tk.id = tkvt.id_tai_khoan
LEFT JOIN vai_tro vt ON tkvt.id_vai_tro = vt.id
WHERE tk.email = 'khach1@gmail.com';
