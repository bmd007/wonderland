CREATE TABLE transactions (
    id              UUID PRIMARY KEY,
    from_account_id UUID NOT NULL REFERENCES accounts(id),
    to_account_id   UUID NOT NULL REFERENCES accounts(id),
    amount          NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency        VARCHAR(3) NOT NULL,
    description     TEXT NOT NULL DEFAULT '',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
