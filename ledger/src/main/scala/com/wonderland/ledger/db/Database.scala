package com.wonderland.ledger.db

import cats.effect.{Async, Resource}
import com.wonderland.ledger.config.DatabaseConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway

object Database:
  def transactor[F[_]: Async](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for
      ec <- ExecutionContexts.fixedThreadPool[F](16)
      xa <- HikariTransactor.newHikariTransactor[F](
        config.driver, config.url, config.user, config.password, ec
      )
    yield xa

  def migrate[F[_]: Async](config: DatabaseConfig): F[Unit] =
    Async[F].delay {
      Flyway.configure()
        .dataSource(config.url, config.user, config.password)
        .load()
        .migrate()
      ()
    }
