package com.wonderland.ledger

import cats.effect.{IO, IOApp, Resource}
import cats.effect.syntax.all.*
import com.comcast.ip4s.*
import com.wonderland.ledger.config.AppConfig
import com.wonderland.ledger.db.Database
import com.wonderland.ledger.pubsub.PgPubSub
import com.wonderland.ledger.repository.*
import com.wonderland.ledger.routes.*
import com.wonderland.ledger.scheduler.TaskScheduler
import com.wonderland.ledger.service.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple:
  private val logger = Slf4jLogger.getLoggerFromName[IO]("Main")

  def run: IO[Unit] =
    AppConfig.load[IO].flatMap(config => appResources(config).use(_ => IO.never))

  private def appResources(config: AppConfig): Resource[IO, Unit] =
    for
      xa <- Database.transactor[IO](config.database)
      _ <- Resource.eval(Database.migrate[IO](config.database))
      accountRepo = AccountRepository()
      transactionRepo = TransactionRepository()
      taskRepo = TaskRepository()
      accountService = AccountService(accountRepo, xa)
      ledgerService = LedgerService(transactionRepo, accountRepo, xa)
      pubSub <- PgPubSub.resource[IO](config.database)
      _ <- Resource.eval(pubSub.listen("new_transaction"))
      _ <- pubSub.stream
        .evalMap(n => logger.info(s"Transaction notification: ${n.payload}"))
        .compile.drain.background
      _ <- TaskScheduler.resource[IO](taskRepo, xa)
      routes = Router(
        "/api/accounts" -> AccountRoutes(accountService).routes,
        "/api/transactions" -> TransactionRoutes(ledgerService).routes
      ).orNotFound
      _ <- EmberServerBuilder.default[IO]
        .withHost(host"0.0.0.0")
        .withPort(Port.fromInt(config.server.port).getOrElse(port"8080"))
        .withHttpApp(routes)
        .build
      _ <- Resource.eval(logger.info(s"Server started on port ${config.server.port}"))
    yield ()
