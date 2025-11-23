# Database Migration Guide

## Problem Description

Your Spring Boot application is encountering DDL errors when trying to alter columns that have unique indexes:

1. **bien_the_san_pham.sku** - Index 'UKfk8ixy5p30a7nm3nofpl2x4gc'
2. **don_hang.so_don_hang** - Index 'UK4tcxu17x6gssxssab673uxm5b'
3. **san_pham.slug** - Index 'UK4dmuoq3hyl5op3ay2mrfhiyu9'

SQL Server requires dropping unique indexes before altering the associated columns.

## Solution

### Option 1: Run Automatic Migration Script

#### For Windows:

```cmd
cd auro
migrate-database.bat
```

#### For Linux/Mac:

```bash
cd auro
chmod +x migrate-database.sh
./migrate-database.sh
```

### Option 2: Manual SQL Execution

If the automatic script fails, run the SQL script manually:

1. Open SQL Server Management Studio (SSMS)
2. Connect to your SQL Server instance
3. Select the `auro1` database
4. Open and execute: `./db/migrations/004_fix_unique_index_conflicts.sql`

### Option 3: Alternative Hibernate Configuration

The application.properties has been updated to use `hibernate.ddl-auto=validate` instead of `update` to prevent automatic schema changes that cause conflicts.

## Configuration Changes Made

### 1. application.properties

- Changed `hibernate.ddl-auto` from `update` to `validate`
- Added Redis configuration to disable Redis repositories (fixes warnings)
- Added additional Hibernate properties for better schema handling

### 2. AuroApplication.java

- Added `@EnableJpaRepositories` to properly configure JPA repository scanning
- This helps separate JPA repositories from potential Redis repositories

## After Migration

Once the migration is complete:

1. **Start your application normally**
2. **Monitor the logs** - you should no longer see the DDL errors
3. **Optional**: You can change back to `hibernate.ddl-auto=update` if needed, but `validate` is safer for production

## Files Created/Modified

- `db/migrations/004_fix_unique_index_conflicts.sql` - SQL migration script
- `migrate-database.bat` - Windows migration script
- `migrate-database.sh` - Linux/Mac migration script
- `src/main/resources/application.properties` - Updated Hibernate and Redis config
- `src/main/java/com/auro/auro/AuroApplication.java` - Added JPA repository configuration

## Troubleshooting

### If migration script fails:

1. Check SQL Server is running
2. Verify database credentials in the script
3. Ensure `sqlcmd` is installed and in your PATH
4. Run the SQL file manually in SSMS

### If you still get Redis warnings:

The warnings are informational and won't prevent startup. They're now disabled with `spring.data.redis.repositories.enabled=false`.

### If you need to use Redis:

1. Remove `spring.data.redis.repositories.enabled=false`
2. Add proper Redis configuration
3. Create Redis-specific repository interfaces that extend appropriate Redis repository classes

## Prevention

To prevent similar issues in the future:

1. Use database migrations (like Flyway or Liquibase) instead of `hibernate.ddl-auto=update`
2. Test schema changes in development first
3. Consider using `validate` or `none` for production environments
