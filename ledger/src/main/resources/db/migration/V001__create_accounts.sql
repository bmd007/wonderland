CREATE TABLE accounts (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    balance    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency   VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_currency ON accounts(currency);
