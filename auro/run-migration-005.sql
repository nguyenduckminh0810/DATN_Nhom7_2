-- Script để chạy migration 005: Thêm cột email_nguoi_huy
-- Chạy script này trong SQL Server Management Studio hoặc Azure Data Studio

USE [AURO_DB];  -- Thay tên database nếu khác
GO

-- Kiểm tra xem cột đã tồn tại chưa
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'don_hang' AND COLUMN_NAME = 'email_nguoi_huy'
)
BEGIN
    PRINT 'Adding column email_nguoi_huy to don_hang table...'
    
    ALTER TABLE don_hang
    ADD email_nguoi_huy NVARCHAR(150) NULL;
    
    -- Thêm comment cho cột
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', 
        @value = N'Email người hủy đơn hàng', 
        @level0type = N'SCHEMA', @level0name = N'dbo', 
        @level1type = N'TABLE', @level1name = N'don_hang', 
        @level2type = N'COLUMN', @level2name = N'email_nguoi_huy';
    
    PRINT 'Column email_nguoi_huy added successfully!'
END
ELSE
BEGIN
    PRINT 'Column email_nguoi_huy already exists. Skipping...'
END
GO
