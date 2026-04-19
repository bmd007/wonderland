package com.wonderland.ledger.repository

import cats.effect.IO
import cats.syntax.all.*
import com.wonderland.ledger.PostgresSpec
import com.wonderland.ledger.db.Database
import com.wonderland.ledger.domain.*
import doobie.implicits.*
import java.time.Instant

class TaskRepositorySpec extends PostgresSpec:
  import TaskRepository.given

  private val repo = TaskRepository()

  test("schedule and claim task") {
    withContainers { container =>
      val xa = transactorFor(container)
      for
        _ <- Database.migrate[IO](databaseConfig(container))
        task <- repo.schedule(
          CreateTask("reconciliation", """{"accountId": "123"}""", Instant.now().minusSeconds(60))
        ).transact(xa)
        claimed <- repo.claimNext.transact(xa)
      yield
        assert(claimed.isDefined)
        assertEquals(claimed.get.id, task.id)
        assertEquals(claimed.get.status, TaskStatus.Running)
    }
  }

  test("claim multiple tasks sequentially") {
    withContainers { container =>
      val xa = transactorFor(container)
      for
        _ <- Database.migrate[IO](databaseConfig(container))
        _ <- repo.schedule(CreateTask("task-1", "{}", Instant.now().minusSeconds(60))).transact(xa)
        _ <- repo.schedule(CreateTask("task-2", "{}", Instant.now().minusSeconds(30))).transact(xa)
        first <- repo.claimNext.transact(xa)
        second <- repo.claimNext.transact(xa)
        third <- repo.claimNext.transact(xa)
      yield
        assert(first.isDefined)
        assert(second.isDefined)
        assert(third.isEmpty)
    }
  }

  test("complete task removes it from pending") {
    withContainers { container =>
      val xa = transactorFor(container)
      for
        _ <- Database.migrate[IO](databaseConfig(container))
        task <- repo.schedule(
          CreateTask("cleanup", "{}", Instant.now().minusSeconds(60))
        ).transact(xa)
        _ <- (repo.claimNext *> repo.complete(task.id)).transact(xa)
        pending <- repo.findPending.transact(xa)
      yield
        assert(pending.isEmpty)
    }
  }
