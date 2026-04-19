package com.wonderland.ledger.routes

import cats.syntax.all.*
import cats.effect.Concurrent
import com.wonderland.ledger.domain.*
import com.wonderland.ledger.service.LedgerService
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.dsl.Http4sDsl

class TransactionRoutes[F[_]: Concurrent](service: LedgerService[F]) extends Http4sDsl[F]:
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "account" / UUIDVar(accountId) =>
      service.findByAccountId(accountId).flatMap(Ok(_))

    case req @ POST -> Root / "transfer" =>
      req.as[CreateTransaction].flatMap { cmd =>
        service.transfer(cmd)
          .flatMap(Created(_))
          .handleErrorWith {
            case _: AccountNotFound         => NotFound()
            case e: InsufficientBalance     => UnprocessableEntity(e.getMessage)
            case e: IncompatibleCurrencies   => BadRequest(e.getMessage)
          }
      }
  }
