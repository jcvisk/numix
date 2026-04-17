-- Introduce account model and email-based login while preserving rol_user (many-to-many).

CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

INSERT INTO app_role (code, description)
VALUES
    ('OWNER', 'Account owner'),
    ('ADMIN', 'Account administrator'),
    ('AUX', 'Auxiliary account user')
ON CONFLICT (code) DO NOTHING;

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS full_name VARCHAR(120);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS account_id BIGINT;

UPDATE app_user
SET email = CASE
    WHEN username = 'admin' THEN 'superadmin@numix.local'
    ELSE LOWER(username)
END
WHERE email IS NULL;

UPDATE app_user
SET full_name = COALESCE(NULLIF(TRIM(username), ''), 'Usuario')
WHERE full_name IS NULL;

ALTER TABLE app_user ALTER COLUMN email SET NOT NULL;
ALTER TABLE app_user ALTER COLUMN full_name SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_app_user_account'
    ) THEN
        ALTER TABLE app_user
            ADD CONSTRAINT fk_app_user_account
            FOREIGN KEY (account_id)
            REFERENCES account(id)
            ON DELETE SET NULL;
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_app_user_email'
    ) THEN
        ALTER TABLE app_user
            ADD CONSTRAINT uk_app_user_email UNIQUE (email);
    END IF;
END
$$;
