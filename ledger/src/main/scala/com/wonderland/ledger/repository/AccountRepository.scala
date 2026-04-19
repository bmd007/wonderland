package com.wonderland.ledger.repository

import cats.syntax.all.*
import com.wonderland.ledger.domain.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import java.util.UUID

class AccountRepository:
  def create(cmd: CreateAccount): ConnectionIO[Account] =
    val id = UUID.randomUUID()
    sql"""
      INSERT INTO accounts (id, name, balance, currency)
      VALUES ($id, ${cmd.name}, 0, ${cmd.currency})
      RETURNING id, name, balance, currency, created_at
    """.query[Account].unique

  def findById(id: UUID): ConnectionIO[Option[Account]] =
    sql"SELECT id, name, balance, currency, created_at FROM accounts WHERE id = $id"
      .query[Account].option

  def findByIdForUpdate(id: UUID): ConnectionIO[Option[Account]] =
    sql"SELECT id, name, balance, currency, created_at FROM accounts WHERE id = $id FOR UPDATE"
      .query[Account].option

  def findAll: ConnectionIO[List[Account]] =
    sql"SELECT id, name, balance, currency, created_at FROM accounts ORDER BY created_at DESC"
      .query[Account].to[List]

  def updateBalance(id: UUID, newBalance: BigDecimal): ConnectionIO[Unit] =
    sql"UPDATE accounts SET balance = $newBalance WHERE id = $id"
      .update.run.void
