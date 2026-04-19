CREATE TABLE accounts (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    balance    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
