CREATE OR REPLACE FUNCTION notify_new_transaction() RETURNS trigger AS $$
BEGIN
    PERFORM pg_notify(
        'new_transaction',
        json_build_object(
            'id', NEW.id,
            'from_account_id', NEW.from_account_id,
            'to_account_id', NEW.to_account_id,
            'amount', NEW.amount,
            'currency', NEW.currency
        )::text
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_new_transaction
    AFTER INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION notify_new_transaction();
