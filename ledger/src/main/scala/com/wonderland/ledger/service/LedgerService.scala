package com.wonderland.ledger.service

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all.*
import com.wonderland.ledger.domain.*
import com.wonderland.ledger.repository.{AccountRepository, TransactionRepository}
import doobie.*
import doobie.implicits.*
import java.util.UUID

class LedgerService[F[_]: MonadCancelThrow](
    transactionRepo: TransactionRepository,
    accountRepo: AccountRepository,
    xa: Transactor[F]
):
  def transfer(cmd: CreateTransaction): F[Transaction] =
    val op = for
      from <- accountRepo.findByIdForUpdate(cmd.fromAccountId)
        .map(_.getOrElse(throw AccountNotFound(cmd.fromAccountId)))
      to <- accountRepo.findByIdForUpdate(cmd.toAccountId)
        .map(_.getOrElse(throw AccountNotFound(cmd.toAccountId)))
      _ <- FC.delay(validate(from, to, cmd))
      _ <- accountRepo.updateBalance(from.id, from.balance - cmd.amount)
      _ <- accountRepo.updateBalance(to.id, to.balance + cmd.amount)
      txn <- transactionRepo.create(cmd, from.currency)
    yield txn
    op.transact(xa)

  def findByAccountId(accountId: UUID): F[List[Transaction]] =
    transactionRepo.findByAccountId(accountId).transact(xa)

  private def validate(from: Account, to: Account, cmd: CreateTransaction): Unit =
    if from.currency != to.currency then
      throw IncompatibleCurrencies(from.currency, to.currency)
    if from.balance < cmd.amount then
      throw InsufficientBalance(from.id, cmd.amount, from.balance)
