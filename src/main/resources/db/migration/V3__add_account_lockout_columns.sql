-- Account lockout is intentionally not part of this POC.
-- Keep this migration version so a fresh local database also removes any
-- columns created by an earlier version of the POC.
ALTER TABLE users
    DROP COLUMN IF EXISTS failed_login_attempts,
    DROP COLUMN IF EXISTS lockout_until;
