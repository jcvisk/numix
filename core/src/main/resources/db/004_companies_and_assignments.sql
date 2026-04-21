CREATE TABLE IF NOT EXISTS currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    CONSTRAINT uk_currencies_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    legal_name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(50) NOT NULL,
    fiscal_regime VARCHAR(100) NOT NULL,
    tax_zip_code VARCHAR(10),
    base_currency_id BIGINT NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_companies_tax_id UNIQUE (tax_id),
    CONSTRAINT fk_companies_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    CONSTRAINT fk_companies_currency FOREIGN KEY (base_currency_id) REFERENCES currencies(id),
    CONSTRAINT chk_companies_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE IF NOT EXISTS user_companies (
    user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by BIGINT,
    CONSTRAINT pk_user_companies PRIMARY KEY (user_id, company_id),
    CONSTRAINT fk_user_companies_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_companies_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_companies_assigned_by FOREIGN KEY (assigned_by) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_companies_account_id ON companies(account_id);
CREATE INDEX IF NOT EXISTS idx_user_companies_company_id ON user_companies(company_id);
CREATE INDEX IF NOT EXISTS idx_user_companies_assigned_by ON user_companies(assigned_by);

INSERT INTO currencies (code, name, symbol)
VALUES
    ('MXN', 'Peso mexicano', '$'),
    ('USD', 'US Dollar', '$')
ON CONFLICT (code) DO NOTHING;
