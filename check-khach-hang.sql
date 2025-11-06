-- Kiểm tra xem user hiện tại có trong bảng khach_hang không
-- Thay 'khach1@gmail.com' bằng email tài khoản bạn đang dùng

SELECT 
    tk.id as tai_khoan_id,
    tk.email,
    tk.ho_ten,
    kh.id as khach_hang_id,
    kh.dia_chi,
    kh.so_dien_thoai,
    CASE 
        WHEN kh.id IS NULL THEN 'KHÔNG CÓ RECORD KHÁCH HÀNG - ĐÂY LÀ VẤN ĐỀ!'
        ELSE 'OK - Có record khách hàng'
    END as status
FROM auro1.tai_khoan tk
LEFT JOIN auro1.khach_hang kh ON kh.tai_khoan_id = tk.id
WHERE tk.email = 'khach1@gmail.com'  -- Thay bằng email của bạn
;

-- Kiểm tra tất cả các tài khoản có vai trò KHÁCH HÀNG
SELECT 
    tk.id as tai_khoan_id,
    tk.email,
    tk.ho_ten,
    vt.ten_vai_tro,
    kh.id as khach_hang_id,
    CASE 
        WHEN kh.id IS NULL THEN '❌ THIẾU RECORD KHÁCH HÀNG'
        ELSE '✅ OK'
    END as status
FROM auro1.tai_khoan tk
JOIN auro1.tai_khoan_vai_tro tkvt ON tk.id = tkvt.tai_khoan_id
JOIN auro1.vai_tro vt ON tkvt.vai_tro_id = vt.id
LEFT JOIN auro1.khach_hang kh ON kh.tai_khoan_id = tk.id
WHERE vt.ten_vai_tro = 'ROLE_CUS'
ORDER BY tk.id;

-- Xem tất cả địa chỉ trong DB
SELECT 
    dc.id,
    dc.ho_ten,
    dc.dia_chi_1,
    dc.tinh_thanh,
    dc.mac_dinh,
    dc.id_khach_hang,
    kh.tai_khoan_id,
    tk.email
FROM auro1.dia_chi dc
JOIN auro1.khach_hang kh ON dc.id_khach_hang = kh.id
JOIN auro1.tai_khoan tk ON kh.tai_khoan_id = tk.id
ORDER BY dc.id DESC
LIMIT 20;
