-- Centralize status values in catalog table app_status and migrate account.status to account.status_id.

CREATE TABLE IF NOT EXISTS app_status (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(120) NOT NULL,
    CONSTRAINT uk_app_status_code UNIQUE (code)
);

INSERT INTO app_status (code, name)
VALUES
    ('ACTIVE', 'Activo'),
    ('SUSPENDED', 'Suspendido')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name;

ALTER TABLE account ADD COLUMN IF NOT EXISTS status_id BIGINT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'account'
          AND column_name = 'status'
    ) THEN
        EXECUTE '
            UPDATE account a
            SET status_id = s.id
            FROM app_status s
            WHERE a.status_id IS NULL
              AND s.code = COALESCE(a.status, ''ACTIVE'')
        ';
    END IF;
END $$;

UPDATE account a
SET status_id = s.id
FROM app_status s
WHERE a.status_id IS NULL
  AND s.code = 'ACTIVE';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'account'
          AND column_name = 'status_id'
          AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE account ALTER COLUMN status_id SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_account_status'
    ) THEN
        ALTER TABLE account
            ADD CONSTRAINT fk_account_status
            FOREIGN KEY (status_id)
            REFERENCES app_status(id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_account_status'
    ) THEN
        ALTER TABLE account DROP CONSTRAINT chk_account_status;
    END IF;
END $$;

ALTER TABLE account DROP COLUMN IF EXISTS status;
