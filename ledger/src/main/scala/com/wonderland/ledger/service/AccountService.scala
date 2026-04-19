package com.wonderland.ledger.service

import cats.effect.kernel.MonadCancelThrow
import com.wonderland.ledger.domain.*
import com.wonderland.ledger.repository.AccountRepository
import doobie.Transactor
import doobie.implicits.*
import java.util.UUID

class AccountService[F[_]: MonadCancelThrow](repo: AccountRepository, xa: Transactor[F]):
  def create(cmd: CreateAccount): F[Account] =
    repo.create(cmd).transact(xa)

  def findById(id: UUID): F[Option[Account]] =
    repo.findById(id).transact(xa)

  def findAll: F[List[Account]] =
    repo.findAll.transact(xa)
