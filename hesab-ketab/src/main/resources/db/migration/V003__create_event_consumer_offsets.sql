CREATE TABLE event_consumer_offsets (
    consumer_name  VARCHAR(100) PRIMARY KEY,
    last_sequence  BIGINT NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
