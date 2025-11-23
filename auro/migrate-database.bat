@echo off
REM Database Migration Script for Windows
REM Run this script BEFORE starting the Spring Boot application

echo Starting database migration to fix unique index conflicts...

REM Database connection details
set DB_SERVER=localhost
set DB_NAME=auro1
set DB_USER=sa
set DB_PASSWORD=123

REM Check if sqlcmd is available
where sqlcmd >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: sqlcmd is not available. Please install SQL Server command line tools.
    echo Alternatively, run the migration script manually using SQL Server Management Studio.
    echo Migration file: .\db\migrations\004_fix_unique_index_conflicts.sql
    pause
    exit /b 1
)

REM Run the migration script
echo Executing migration script...
sqlcmd -S "%DB_SERVER%" -d "%DB_NAME%" -U "%DB_USER%" -P "%DB_PASSWORD%" -i ".\db\migrations\004_fix_unique_index_conflicts.sql"

if %errorlevel% equ 0 (
    echo Migration completed successfully!
    echo You can now start the Spring Boot application.
) else (
    echo Migration failed. Please check the error messages above.
    echo You may need to run the SQL script manually.
    pause
    exit /b 1
)

pause