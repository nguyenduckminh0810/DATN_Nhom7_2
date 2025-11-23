#!/bin/bash

# Database Migration Script
# Run this script BEFORE starting the Spring Boot application

echo "Starting database migration to fix unique index conflicts..."

# Database connection details
DB_SERVER="localhost"
DB_NAME="auro1"
DB_USER="sa"
DB_PASSWORD="123"

# Check if sqlcmd is available
if ! command -v sqlcmd &> /dev/null; then
    echo "Error: sqlcmd is not available. Please install SQL Server command line tools."
    echo "Alternatively, run the migration script manually using SQL Server Management Studio."
    echo "Migration file: ./db/migrations/004_fix_unique_index_conflicts.sql"
    exit 1
fi

# Run the migration script
echo "Executing migration script..."
sqlcmd -S "$DB_SERVER" -d "$DB_NAME" -U "$DB_USER" -P "$DB_PASSWORD" -i "./db/migrations/004_fix_unique_index_conflicts.sql"

if [ $? -eq 0 ]; then
    echo "Migration completed successfully!"
    echo "You can now start the Spring Boot application."
else
    echo "Migration failed. Please check the error messages above."
    echo "You may need to run the SQL script manually."
    exit 1
fi