package com.wonderland.ledger.pubsub

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import com.wonderland.ledger.config.DatabaseConfig
import fs2.Stream
import org.postgresql.PGConnection
import java.sql.DriverManager
import scala.concurrent.duration.*

case class PgNotification(channel: String, payload: String)

class PgPubSub[F[_]: Async] private (pgConn: PGConnection):
  private val jdbc = pgConn.asInstanceOf[java.sql.Connection]

  def listen(channel: String): F[Unit] =
    Async[F].blocking {
      val stmt = jdbc.createStatement()
      stmt.execute(s"LISTEN $channel")
      stmt.close()
    }

  def notify(channel: String, payload: String): F[Unit] =
    Async[F].blocking {
      val stmt = jdbc.createStatement()
      stmt.execute(s"NOTIFY $channel, '${payload.replace("'", "''")}'")
      stmt.close()
    }

  def stream: Stream[F, PgNotification] =
    Stream
      .awakeEvery[F](200.millis)
      .evalMap(_ => poll)
      .flatMap(Stream.emits)

  private def poll: F[List[PgNotification]] =
    Async[F].blocking {
      val raw = pgConn.getNotifications(100)
      if raw == null then Nil
      else raw.toList.map(n => PgNotification(n.getName, n.getParameter))
    }

object PgPubSub:
  def resource[F[_]: Async](config: DatabaseConfig): Resource[F, PgPubSub[F]] =
    Resource
      .fromAutoCloseable(Async[F].blocking {
        DriverManager.getConnection(config.url, config.user, config.password)
      })
      .map(conn => new PgPubSub(conn.unwrap(classOf[PGConnection])))
