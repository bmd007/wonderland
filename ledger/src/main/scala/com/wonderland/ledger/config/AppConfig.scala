package com.wonderland.ledger.config

import cats.effect.Sync
import com.typesafe.config.ConfigFactory

case class ServerConfig(port: Int)

case class DatabaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String
)

case class AppConfig(server: ServerConfig, database: DatabaseConfig)

object AppConfig:
  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay {
    val cfg = ConfigFactory.load()
    AppConfig(
      server = ServerConfig(cfg.getInt("server.port")),
      database = DatabaseConfig(
        driver = cfg.getString("database.driver"),
        url = cfg.getString("database.url"),
        user = cfg.getString("database.user"),
        password = cfg.getString("database.password")
      )
    )
  }
