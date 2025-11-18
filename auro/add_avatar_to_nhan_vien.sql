-- Add avatar column to nhan_vien table for Admin and Staff

-- Check if column exists before adding
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'nhan_vien' AND COLUMN_NAME = 'avatar'
)
BEGIN
    ALTER TABLE nhan_vien ADD avatar NVARCHAR(500) NULL;
    PRINT 'Column avatar added to nhan_vien successfully';
END
ELSE
BEGIN
    PRINT 'Column avatar already exists in nhan_vien';
END
