-- Migration to fix unique index conflicts with column alterations
-- Run this BEFORE starting the Spring Boot application

-- Fix bien_the_san_pham.sku column
-- Drop the existing unique constraint/index
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'UKfk8ixy5p30a7nm3nofpl2x4gc' AND object_id = OBJECT_ID('bien_the_san_pham'))
BEGIN
    DROP INDEX UKfk8ixy5p30a7nm3nofpl2x4gc ON bien_the_san_pham;
END
GO

-- Alter the column
IF COL_LENGTH('bien_the_san_pham', 'sku') IS NOT NULL
BEGIN
    ALTER TABLE bien_the_san_pham ALTER COLUMN sku NVARCHAR(100);
END
GO

-- Recreate the unique constraint
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UKfk8ixy5p30a7nm3nofpl2x4gc' AND object_id = OBJECT_ID('bien_the_san_pham'))
BEGIN
    ALTER TABLE bien_the_san_pham ADD CONSTRAINT UKfk8ixy5p30a7nm3nofpl2x4gc UNIQUE (sku);
END
GO

-- Fix don_hang.so_don_hang column
-- Drop the existing unique constraint/index
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'UK4tcxu17x6gssxssab673uxm5b' AND object_id = OBJECT_ID('don_hang'))
BEGIN
    DROP INDEX UK4tcxu17x6gssxssab673uxm5b ON don_hang;
END
GO

-- Alter the column
IF COL_LENGTH('don_hang', 'so_don_hang') IS NOT NULL
BEGIN
    ALTER TABLE don_hang ALTER COLUMN so_don_hang NVARCHAR(50);
END
GO

-- Recreate the unique constraint
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UK4tcxu17x6gssxssab673uxm5b' AND object_id = OBJECT_ID('don_hang'))
BEGIN
    ALTER TABLE don_hang ADD CONSTRAINT UK4tcxu17x6gssxssab673uxm5b UNIQUE (so_don_hang);
END
GO

-- Fix san_pham.slug column
-- Drop the existing unique constraint/index
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'UK4dmuoq3hyl5op3ay2mrfhiyu9' AND object_id = OBJECT_ID('san_pham'))
BEGIN
    DROP INDEX UK4dmuoq3hyl5op3ay2mrfhiyu9 ON san_pham;
END
GO

-- Alter the column
IF COL_LENGTH('san_pham', 'slug') IS NOT NULL
BEGIN
    ALTER TABLE san_pham ALTER COLUMN slug NVARCHAR(200);
END
GO

-- Recreate the unique constraint
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UK4dmuoq3hyl5op3ay2mrfhiyu9' AND object_id = OBJECT_ID('san_pham'))
BEGIN
    ALTER TABLE san_pham ADD CONSTRAINT UK4dmuoq3hyl5op3ay2mrfhiyu9 UNIQUE (slug);
END
GO

PRINT 'Migration completed successfully - unique index conflicts resolved';