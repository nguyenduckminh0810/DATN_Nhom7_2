-- Kiểm tra chi tiết user khach1@gmail.com
SELECT 
    nd.id as nguoi_dung_id,
    nd.email,
    nd.ho_ten,
    vt.id as vai_tro_id,
    vt.ma_vai_tro,
    vt.ten_vai_tro,
    kh.id as khach_hang_id
FROM nguoi_dung nd
LEFT JOIN nguoi_dung_vai_tro ndvt ON nd.id = ndvt.nguoi_dung_id
LEFT JOIN vai_tro vt ON ndvt.vai_tro_id = vt.id
LEFT JOIN khach_hang kh ON nd.id = kh.nguoi_dung_id
WHERE nd.email = 'khach1@gmail.com';

-- Xem tất cả vai_tro có trong hệ thống
SELECT * FROM vai_tro;

-- Nếu user chưa có role CUS, thêm vào:
-- INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id)
-- VALUES (8, 1);  -- Giả sử vai_tro_id=1 là CUS
