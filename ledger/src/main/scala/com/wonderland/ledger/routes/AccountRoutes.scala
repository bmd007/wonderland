package com.wonderland.ledger.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.wonderland.ledger.domain.*
import com.wonderland.ledger.service.AccountService
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.dsl.Http4sDsl

class AccountRoutes[F[_]: Concurrent](service: AccountService[F]) extends Http4sDsl[F]:
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      service.findAll.flatMap(Ok(_))

    case GET -> Root / UUIDVar(id) =>
      service.findById(id).flatMap {
        case Some(account) => Ok(account)
        case None          => NotFound()
      }

    case req @ POST -> Root =>
      req.as[CreateAccount].flatMap(cmd => service.create(cmd).flatMap(Created(_)))
  }
