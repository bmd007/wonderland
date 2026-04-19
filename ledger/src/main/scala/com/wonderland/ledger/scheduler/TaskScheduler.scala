package com.wonderland.ledger.scheduler

import cats.effect.{Async, Resource}
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.wonderland.ledger.repository.TaskRepository
import doobie.*
import doobie.implicits.*
import fs2.Stream
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration.*

class TaskScheduler[F[_]: Async](taskRepo: TaskRepository, xa: Transactor[F]):
  private val logger = Slf4jLogger.getLoggerFromName[F]("TaskScheduler")

  def stream: Stream[F, Unit] =
    Stream
      .awakeEvery[F](5.seconds)
      .evalMap(_ => claimAndProcess)

  private def claimAndProcess: F[Unit] =
    val op = for
      taskOpt <- taskRepo.claimNext
      _ <- taskOpt.traverse_(task => taskRepo.complete(task.id))
    yield ()
    op.transact(xa).handleErrorWith(err => logger.error(err)("Task processing failed"))

object TaskScheduler:
  def resource[F[_]: Async](taskRepo: TaskRepository, xa: Transactor[F]): Resource[F, Unit] =
    val scheduler = TaskScheduler(taskRepo, xa)
    scheduler.stream.compile.drain.background.void
