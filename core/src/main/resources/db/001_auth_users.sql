-- Users table for Spring Security authentication
CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS app_role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS rol_user (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_rol_user_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_rol_user_role FOREIGN KEY (role_id) REFERENCES app_role(id) ON DELETE CASCADE
);

-- Initial global admin user (password: Admin123!)
INSERT INTO app_user (username, password_hash, enabled)
VALUES ('admin', '$2y$10$Hi/2XOrkI1CY0zeYyZ/PwOb5UaREf295Awfe9vuD1xSJ4ih1Z4GE2', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Default global role for bootstrap admin
INSERT INTO app_role (code, description)
VALUES ('SUPER_ADMIN', 'Global super administrator')
ON CONFLICT (code) DO NOTHING;

-- Ensure bootstrap admin has SUPER_ADMIN
INSERT INTO rol_user (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN app_role r ON r.code = 'SUPER_ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;
