-- FIX: Tạo record khách hàng nếu chưa có
-- Chạy script này nếu tài khoản có role CUSTOMER nhưng chưa có record trong bảng khach_hang

INSERT INTO auro1.khach_hang (tai_khoan_id, ho_ten, so_dien_thoai, dia_chi, ngay_tao, ngay_cap_nhat)
SELECT 
    tk.id,
    tk.ho_ten,
    '0123456789', -- số điện thoại mặc định
    'Chưa cập nhật', -- địa chỉ mặc định
    NOW(),
    NOW()
FROM auro1.tai_khoan tk
JOIN auro1.tai_khoan_vai_tro tkvt ON tk.id = tkvt.tai_khoan_id
JOIN auro1.vai_tro vt ON tkvt.vai_tro_id = vt.id
LEFT JOIN auro1.khach_hang kh ON kh.tai_khoan_id = tk.id
WHERE vt.ten_vai_tro = 'ROLE_CUS'
  AND kh.id IS NULL  -- Chưa có record khách hàng
;

-- Kiểm tra lại
SELECT 
    tk.email,
    tk.ho_ten,
    kh.id as khach_hang_id,
    CASE 
        WHEN kh.id IS NULL THEN '❌ VẪN THIẾU'
        ELSE '✅ ĐÃ CÓ'
    END as status
FROM auro1.tai_khoan tk
JOIN auro1.tai_khoan_vai_tro tkvt ON tk.id = tkvt.tai_khoan_id
JOIN auro1.vai_tro vt ON tkvt.vai_tro_id = vt.id
LEFT JOIN auro1.khach_hang kh ON kh.tai_khoan_id = tk.id
WHERE vt.ten_vai_tro = 'ROLE_CUS';
