CREATE OR REPLACE FUNCTION notify_domain_event() RETURNS trigger AS $$
BEGIN
    PERFORM pg_notify(
        'domain_event',
        json_build_object(
            'sequence_number', NEW.sequence_number,
            'aggregate_id', NEW.aggregate_id,
            'event_type', NEW.event_type
        )::text
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_domain_event
    AFTER INSERT ON domain_events
    FOR EACH ROW
    EXECUTE FUNCTION notify_domain_event();
