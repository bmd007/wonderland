CREATE INDEX idx_domain_events_aggregate_created
    ON domain_events(aggregate_id, created_at);
