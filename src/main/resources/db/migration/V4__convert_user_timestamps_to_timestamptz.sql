-- User timestamps were previously stored as LocalDateTime values.
-- Interpret the existing values as UTC before converting them to absolute timestamps.
ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN last_login_at TYPE TIMESTAMPTZ
        USING last_login_at AT TIME ZONE 'UTC';
