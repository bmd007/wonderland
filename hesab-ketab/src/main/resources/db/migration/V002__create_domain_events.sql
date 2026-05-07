CREATE TABLE domain_events
(
    id                UUID PRIMARY KEY,
    aggregate_id      UUID         NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    payload           JSONB        NOT NULL,
    aggregate_version BIGINT       NOT NULL,
    sequence_number   BIGSERIAL    NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (aggregate_id, aggregate_version)
);

CREATE INDEX idx_domain_events_aggregate ON domain_events (aggregate_id, aggregate_version);
CREATE INDEX idx_domain_events_sequence ON domain_events (sequence_number);
CREATE INDEX idx_domain_events_transaction_id ON domain_events ((payload ->> 'transactionId'));
