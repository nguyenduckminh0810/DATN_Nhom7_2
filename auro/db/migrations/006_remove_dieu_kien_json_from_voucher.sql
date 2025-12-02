IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE Name = N'dieu_kien_json'
      AND Object_ID = Object_ID(N'voucher')
)
BEGIN
    ALTER TABLE voucher DROP COLUMN dieu_kien_json;
END;


