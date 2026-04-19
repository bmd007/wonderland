package io.github.bmd007.wonderland.hesab_ketab.repository;

import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTaskRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ScheduledTaskRepository {

    private final JdbcClient jdbc;

    public ScheduledTaskRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // JSONB casting: payload is stored as JSONB but read as text
    public ScheduledTask schedule(CreateTaskRequest request) {
        var id = UUID.randomUUID();
        return jdbc.sql("""
                INSERT INTO scheduled_tasks (id, task_type, payload, status, scheduled_at)
                VALUES (:id, :taskType, :payload::jsonb, 'pending', :scheduledAt)
                RETURNING id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
                """)
            .param("id", id)
            .param("taskType", request.taskType())
            .param("payload", request.payload())
            .param("scheduledAt", request.scheduledAt())
            .query(ScheduledTask.class)
            .single();
    }

    // FOR UPDATE SKIP LOCKED: non-blocking concurrent task claiming
    // Multiple workers can safely claim different tasks simultaneously
    public Optional<ScheduledTask> claimNext() {
        return jdbc.sql("""
                UPDATE scheduled_tasks
                SET status = 'running', locked_at = now()
                WHERE id = (
                    SELECT id FROM scheduled_tasks
                    WHERE status = 'pending' AND scheduled_at <= now()
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
        jdbc.sql("UPDATE scheduled_tasks SET status = 'completed', completed_at = now() WHERE id = :id")
            .param("id", id)
            .update();
    }

    public void fail(UUID id, String error) {
        jdbc.sql("UPDATE scheduled_tasks SET status = 'failed', completed_at = now(), error_message = :error WHERE id = :id")
            .param("error", error)
            .param("id", id)
            .update();
    }

    public List<ScheduledTask> findPending() {
        return jdbc.sql("""
                SELECT id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
                FROM scheduled_tasks
                WHERE status = 'pending'
                ORDER BY scheduled_at
                """)
            .query(ScheduledTask.class)
            .list();
    }
}
