package com.wonderland.ledger

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainerForAll
import com.wonderland.ledger.config.DatabaseConfig
import doobie.Transactor
import munit.CatsEffectSuite
import org.testcontainers.utility.DockerImageName

trait PostgresSpec extends CatsEffectSuite with TestContainerForAll:
  override val containerDef: PostgreSQLContainer.Def =
    PostgreSQLContainer.Def(DockerImageName.parse("postgres:16-alpine"))

  def databaseConfig(container: PostgreSQLContainer): DatabaseConfig =
    DatabaseConfig(
      driver = container.driverClassName,
      url = container.jdbcUrl,
      user = container.username,
      password = container.password
    )

  def transactorFor(container: PostgreSQLContainer): Transactor[IO] =
    val props = java.util.Properties()
    props.setProperty("user", container.username)
    props.setProperty("password", container.password)
    Transactor.fromDriverManager[IO](
      container.driverClassName,
      container.jdbcUrl,
      props,
      None
    )
