-- Live view: always-consistent balance computed from the event store
CREATE VIEW account_balances AS
SELECT
    a.id,
    a.name,
    a.currency,
    a.created_at,
    COALESCE(
        SUM(CASE WHEN t.to_account_id = a.id THEN t.amount END), 0
    ) - COALESCE(
        SUM(CASE WHEN t.from_account_id = a.id THEN t.amount END), 0
    ) AS balance
FROM accounts a
LEFT JOIN transactions t ON t.from_account_id = a.id OR t.to_account_id = a.id
GROUP BY a.id;

-- Materialized view: fast reads, eventually consistent
-- UNIQUE INDEX enables REFRESH MATERIALIZED VIEW CONCURRENTLY (non-blocking)
CREATE MATERIALIZED VIEW account_balances_cached AS
SELECT * FROM account_balances
WITH DATA;

CREATE UNIQUE INDEX idx_account_balances_cached_id ON account_balances_cached(id);

-- Helper function to refresh the cache without blocking reads
CREATE OR REPLACE FUNCTION refresh_account_balances_cache() RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY account_balances_cached;
END;
$$ LANGUAGE plpgsql;
