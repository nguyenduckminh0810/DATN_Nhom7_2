-- Debug SQL script to check actual order status values in database
-- Run this to see what status values actually exist in don_hang table

-- 1. Check all unique status values in database
SELECT DISTINCT trang_thai, COUNT(*) as count 
FROM don_hang 
GROUP BY trang_thai 
ORDER BY count DESC;

-- 2. Check recent orders with their status and dates
SELECT TOP 10 
    id,
    so_don_hang,
    trang_thai,
    tong_thanh_toan,
    dat_luc,
    tao_luc,
    payment_status,
    payment_method
FROM don_hang 
ORDER BY id DESC;

-- 3. Check orders by dat_luc vs tao_luc
SELECT 
    COUNT(*) as total_orders,
    COUNT(CASE WHEN dat_luc IS NOT NULL THEN 1 END) as has_dat_luc,
    COUNT(CASE WHEN tao_luc IS NOT NULL THEN 1 END) as has_tao_luc,
    COUNT(CASE WHEN trang_thai = 'COMPLETED' THEN 1 END) as completed_status,
    COUNT(CASE WHEN trang_thai LIKE '%hoàn%' OR trang_thai LIKE '%Hoàn%' THEN 1 END) as hoan_tat_status,
    SUM(CASE WHEN trang_thai = 'COMPLETED' THEN tong_thanh_toan ELSE 0 END) as completed_revenue,
    SUM(CASE WHEN trang_thai LIKE '%hoàn%' OR trang_thai LIKE '%Hoàn%' THEN tong_thanh_toan ELSE 0 END) as hoan_tat_revenue
FROM don_hang;

-- 4. Check payment status distribution
SELECT 
    payment_status, 
    COUNT(*) as count,
    SUM(tong_thanh_toan) as total_revenue
FROM don_hang 
GROUP BY payment_status;

-- 5. Check order status and payment combinations
SELECT 
    trang_thai,
    payment_status,
    COUNT(*) as count,
    SUM(tong_thanh_toan) as total_revenue
FROM don_hang 
GROUP BY trang_thai, payment_status
ORDER BY count DESC;