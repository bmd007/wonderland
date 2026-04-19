val Http4sVersion         = "0.23.28"
val DoobieVersion         = "1.0.0-RC5"
val CirceVersion          = "0.14.9"
val FlywayVersion         = "10.17.0"
val TestContainersVersion = "0.41.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ledger",
    version := "0.1.0",
    scalaVersion := "3.5.0",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server"              % Http4sVersion,
      "org.http4s"      %% "http4s-circe"                     % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"                       % Http4sVersion,
      "io.circe"        %% "circe-generic"                    % CirceVersion,
      "io.circe"        %% "circe-parser"                     % CirceVersion,
      "org.tpolecat"    %% "doobie-core"                      % DoobieVersion,
      "org.tpolecat"    %% "doobie-hikari"                    % DoobieVersion,
      "org.tpolecat"    %% "doobie-postgres"                  % DoobieVersion,
      "org.postgresql"   % "postgresql"                       % "42.7.3",
      "com.typesafe"     % "config"                           % "1.4.3",
      "org.flywaydb"     % "flyway-core"                      % FlywayVersion,
      "org.flywaydb"     % "flyway-database-postgresql"       % FlywayVersion,
      "org.typelevel"   %% "log4cats-slf4j"                   % "2.7.0",
      "ch.qos.logback"   % "logback-classic"                  % "1.5.6",
      "org.scalameta"   %% "munit"                            % "1.0.1"              % Test,
      "org.typelevel"   %% "munit-cats-effect"                % "2.0.0"              % Test,
      "com.dimafeng"    %% "testcontainers-scala-munit"       % TestContainersVersion % Test,
      "com.dimafeng"    %% "testcontainers-scala-postgresql"  % TestContainersVersion % Test,
    ),
    testFrameworks += new TestFramework("munit.Framework"),
  )
