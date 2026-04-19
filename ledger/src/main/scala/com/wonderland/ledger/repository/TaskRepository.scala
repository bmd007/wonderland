package com.wonderland.ledger.repository

import cats.syntax.all.*
import com.wonderland.ledger.domain.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import java.util.UUID

class TaskRepository:
  import TaskRepository.given

  def schedule(cmd: CreateTask): ConnectionIO[ScheduledTask] =
    val id = UUID.randomUUID()
    sql"""
      INSERT INTO scheduled_tasks (id, task_type, payload, status, scheduled_at)
      VALUES ($id, ${cmd.taskType}, ${cmd.payload}::jsonb, 'pending', ${cmd.scheduledAt})
      RETURNING id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
    """.query[ScheduledTask].unique

  def claimNext: ConnectionIO[Option[ScheduledTask]] =
    sql"""
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
    """.query[ScheduledTask].option

  def complete(id: UUID): ConnectionIO[Unit] =
    sql"""
      UPDATE scheduled_tasks
      SET status = 'completed', completed_at = now()
      WHERE id = $id
    """.update.run.void

  def fail(id: UUID, error: String): ConnectionIO[Unit] =
    sql"""
      UPDATE scheduled_tasks
      SET status = 'failed', completed_at = now(), error_message = $error
      WHERE id = $id
    """.update.run.void

  def findPending: ConnectionIO[List[ScheduledTask]] =
    sql"""
      SELECT id, task_type, payload::text, status, scheduled_at, locked_at, completed_at, error_message
      FROM scheduled_tasks
      WHERE status = 'pending'
      ORDER BY scheduled_at
    """.query[ScheduledTask].to[List]

object TaskRepository:
  given Meta[TaskStatus] = Meta[String].imap(
    s => TaskStatus.valueOf(s.capitalize)
  )(
    _.toString.toLowerCase
  )
