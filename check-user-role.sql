-- Kiểm tra roles của users trong database
SELECT 
    nd.id,
    nd.email,
    nd.ho_ten,
    GROUP_CONCAT(vt.ma_vai_tro) as roles,
    kh.id as khach_hang_id
FROM nguoi_dung nd
LEFT JOIN nguoi_dung_vai_tro ndvt ON nd.id = ndvt.nguoi_dung_id
LEFT JOIN vai_tro vt ON ndvt.vai_tro_id = vt.id
LEFT JOIN khach_hang kh ON nd.id = kh.nguoi_dung_id
GROUP BY nd.id, nd.email, nd.ho_ten, kh.id
ORDER BY nd.id;

-- Nếu user không có ROLE_CUS, chạy lệnh này (thay USER_ID):
-- INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id)
-- SELECT [USER_ID], id FROM vai_tro WHERE ma_vai_tro = 'CUS';
