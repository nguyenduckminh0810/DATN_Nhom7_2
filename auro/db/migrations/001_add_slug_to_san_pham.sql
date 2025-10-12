-- Migration: add slug column to san_pham (SQL Server)
-- Location: auro/db/migrations/001_add_slug_to_san_pham.sql
-- Notes:
-- 1) This script is written for Microsoft SQL Server (jdbc:sqlserver). It will:
--    - add a nullable NVARCHAR(200) column `slug`
--    - create a UNIQUE filtered index on `slug` for non-NULL values
--    - populate existing NULL slugs with a safe unique value using the product id
-- 2) Review and run in a test/staging environment before applying to production.
-- 3) If you prefer prettier slugs from the `ten` column, see the commented alternative below.

BEGIN TRANSACTION;

-- 1) Add the column (nullable)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE [object_id] = OBJECT_ID(N'dbo.san_pham') AND name = N'slug'
)
BEGIN
    ALTER TABLE dbo.san_pham ADD slug NVARCHAR(200) NULL;
END;

-- 2) Populate existing rows with a safe, unique slug based on id for any NULLs
--    This guarantees uniqueness and avoids complications with special characters or duplicates.
UPDATE dbo.san_pham
SET slug = 'p-' + CAST(id AS NVARCHAR(50))
WHERE slug IS NULL;

-- 3) Create a unique filtered index on slug for non-NULL values (prevents duplicates among non-null slugs)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes idx
    JOIN sys.objects obj ON idx.object_id = obj.object_id
    WHERE obj.name = N'san_pham' AND idx.name = N'UX_san_pham_slug'
)
BEGIN
    CREATE UNIQUE INDEX UX_san_pham_slug ON dbo.san_pham(slug) WHERE slug IS NOT NULL;
END;

COMMIT TRANSACTION;

-- OPTIONAL: nicer slugs based on the `ten` column (best-effort, may need manual review)
-- The SQL below attempts to create a URL-friendly slug from `ten` by replacing spaces with hyphens
-- and lowercasing. It does NOT remove diacritics; for robust results consider generating slugs in the application layer
-- where you can use libraries to transliterate Vietnamese characters.
--
-- UPDATE dbo.san_pham
-- SET slug = LEFT(LOWER(REPLACE(ten, ' ', '-')) + '-' + CAST(id AS NVARCHAR(50)), 200)
-- WHERE slug IS NOT NULL; -- run for rows you want prettier slugs (use a test set first)

-- After running this migration, restart the Spring Boot app so the JPA entity mapping finds the column.
-- If you use versioned migrations (Flyway/Liquibase), copy these statements into your migration framework instead.
