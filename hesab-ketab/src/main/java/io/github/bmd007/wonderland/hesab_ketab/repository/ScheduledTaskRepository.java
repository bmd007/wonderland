package io.github.bmd007.wonderland.hesab_ketab.repository;

import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ScheduledTaskRepository {

    private final JdbcClient jdbc;

    public void scheduleIdempotent(UUID id, String taskType, String payload) {
        jdbc.sql("""
                INSERT INTO scheduled_tasks (id, task_type, payload, status, scheduled_at)
                VALUES (:id, :taskType, :payload::jsonb, 'PENDING', now())
                ON CONFLICT (id) DO NOTHING
                """)
            .param("id", id)
            .param("taskType", taskType)
            .param("payload", payload)
            .update();
    }

    // FOR UPDATE SKIP LOCKED: non-blocking concurrent task claiming
    public Optional<ScheduledTask> claimNext() {
        return jdbc.sql("""
                UPDATE scheduled_tasks
                SET status = 'RUNNING', locked_at = now()
                WHERE id = (
                    SELECT id FROM scheduled_tasks
                    WHERE status = 'PENDING' AND scheduled_at <= now()
                    ORDER BY scheduled_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
                """)
            .query(ScheduledTask.class)
            .optional();
    }

    public void complete(UUID id) {
        jdbc.sql("UPDATE scheduled_tasks SET status = 'COMPLETED', completed_at = now() WHERE id = :id")
            .param("id", id)
            .update();
    }

    public void fail(UUID id, String error) {
        jdbc.sql("UPDATE scheduled_tasks SET status = 'FAILED', completed_at = now(), error_message = :error WHERE id = :id")
            .param("error", error)
            .param("id", id)
            .update();
    }

    public List<ScheduledTask> findAll() {
        return jdbc.sql("""
                SELECT id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
                FROM scheduled_tasks
                ORDER BY created_at DESC
                """)
            .query(ScheduledTask.class)
            .list();
    }

    public List<ScheduledTask> findFailed() {
        return jdbc.sql("""
                SELECT id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
                FROM scheduled_tasks
                WHERE status = 'FAILED'
                ORDER BY completed_at DESC
                """)
            .query(ScheduledTask.class)
            .list();
    }
}
