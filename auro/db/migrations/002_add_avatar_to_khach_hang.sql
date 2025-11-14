-- Migration: Add avatar column to khach_hang table
-- Date: 2025-11-14

-- Add avatar column
ALTER TABLE khach_hang ADD COLUMN avatar NVARCHAR(500);

-- Add comment
COMMENT ON COLUMN khach_hang.avatar IS 'URL của ảnh đại diện khách hàng';
