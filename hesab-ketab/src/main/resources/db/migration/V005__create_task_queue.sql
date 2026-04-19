CREATE TABLE scheduled_tasks (
    id            UUID PRIMARY KEY,
    task_type     VARCHAR(100) NOT NULL,
    payload       JSONB NOT NULL DEFAULT '{}',
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    scheduled_at  TIMESTAMPTZ NOT NULL,
    locked_at     TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_claimable ON scheduled_tasks(scheduled_at)
    WHERE status = 'PENDING';
