-- Migration to remove PROCESSING/CONFIRMED status
-- Convert all PROCESSING/CONFIRMED orders to SHIPPING status

-- Update orders with CONFIRMED status to SHIPPING
UPDATE don_hang 
SET trang_thai = 'SHIPPING',
    cap_nhat_luc = NOW()
WHERE trang_thai = 'CONFIRMED';

-- Update orders with Vietnamese "Đã xác nhận" status to SHIPPING
UPDATE don_hang 
SET trang_thai = 'SHIPPING',
    cap_nhat_luc = NOW()
WHERE trang_thai = 'Đã xác nhận';

-- Update any remaining variations
UPDATE don_hang 
SET trang_thai = 'SHIPPING',
    cap_nhat_luc = NOW()
WHERE trang_thai IN ('PROCESSING', 'Đang xử lý', 'ĐANG XỬ LÝ', 'DANG_XU_LY');

-- Add comment for clarity
COMMENT ON COLUMN don_hang.trang_thai IS 'Order status: PENDING, SHIPPING, COMPLETED, CANCELLED (PROCESSING status removed)';
