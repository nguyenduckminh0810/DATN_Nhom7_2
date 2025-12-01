-- Migration: Thêm cột ly_do_huy vào bảng don_hang
-- Date: 2025-01-12
-- Description: Thêm trường lưu lý do hủy đơn hàng

ALTER TABLE don_hang
ADD ly_do_huy NVARCHAR(500) NULL;

-- Thêm comment cho cột
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Lý do hủy đơn hàng', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'don_hang', 
    @level2type = N'COLUMN', @level2name = N'ly_do_huy';

