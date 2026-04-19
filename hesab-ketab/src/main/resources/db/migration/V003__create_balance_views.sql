-- Projection table: maintained by the Java app via LISTEN/NOTIFY
-- This is the read model in CQRS — eventually consistent with the event store
CREATE TABLE account_balances (
    account_id UUID PRIMARY KEY REFERENCES accounts(id),
    balance    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
