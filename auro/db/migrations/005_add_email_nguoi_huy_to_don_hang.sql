-- Migration: Thêm cột email_nguoi_huy vào bảng don_hang
-- Date: 2025-01-12
-- Description: Thêm trường lưu email người hủy đơn hàng

ALTER TABLE don_hang
ADD email_nguoi_huy NVARCHAR(150) NULL;

-- Thêm comment cho cột
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Email người hủy đơn hàng', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'don_hang', 
    @level2type = N'COLUMN', @level2name = N'email_nguoi_huy';

