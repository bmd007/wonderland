package com.wonderland.ledger.domain

import io.circe.{Codec, Decoder, Encoder}
import java.time.Instant
import java.util.UUID

case class Account(
    id: UUID,
    name: String,
    balance: BigDecimal,
    currency: String,
    createdAt: Instant
) derives Codec.AsObject

case class CreateAccount(
    name: String,
    currency: String
) derives Codec.AsObject

case class Transaction(
    id: UUID,
    fromAccountId: UUID,
    toAccountId: UUID,
    amount: BigDecimal,
    currency: String,
    description: String,
    createdAt: Instant
) derives Codec.AsObject

case class CreateTransaction(
    fromAccountId: UUID,
    toAccountId: UUID,
    amount: BigDecimal,
    description: String
) derives Codec.AsObject

enum TaskStatus:
  case Pending, Running, Completed, Failed

object TaskStatus:
  given Codec[TaskStatus] = Codec.from(
    Decoder[String].map(s => TaskStatus.valueOf(s.capitalize)),
    Encoder[String].contramap(_.toString.toLowerCase)
  )

case class ScheduledTask(
    id: UUID,
    taskType: String,
    payload: String,
    status: TaskStatus,
    scheduledAt: Instant,
    lockedAt: Option[Instant],
    completedAt: Option[Instant],
    errorMessage: Option[String]
) derives Codec.AsObject

case class CreateTask(
    taskType: String,
    payload: String,
    scheduledAt: Instant
) derives Codec.AsObject

sealed trait LedgerError extends RuntimeException

case class AccountNotFound(accountId: UUID) extends LedgerError:
  override def getMessage: String = s"Account not found: $accountId"

case class InsufficientBalance(accountId: UUID, requested: BigDecimal, available: BigDecimal) extends LedgerError:
  override def getMessage: String =
    s"Insufficient balance in $accountId: requested $requested, available $available"

case class IncompatibleCurrencies(from: String, to: String) extends LedgerError:
  override def getMessage: String = s"Cannot transfer between different currencies: $from and $to"
