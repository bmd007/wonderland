package com.wonderland.ledger.repository

import com.wonderland.ledger.domain.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import java.util.UUID

class TransactionRepository:
  def create(cmd: CreateTransaction, currency: String): ConnectionIO[Transaction] =
    val id = UUID.randomUUID()
    sql"""
      INSERT INTO transactions (id, from_account_id, to_account_id, amount, currency, description)
      VALUES ($id, ${cmd.fromAccountId}, ${cmd.toAccountId}, ${cmd.amount}, $currency, ${cmd.description})
      RETURNING id, from_account_id, to_account_id, amount, currency, description, created_at
    """.query[Transaction].unique

  def findByAccountId(accountId: UUID): ConnectionIO[List[Transaction]] =
    sql"""
      SELECT id, from_account_id, to_account_id, amount, currency, description, created_at
      FROM transactions
      WHERE from_account_id = $accountId OR to_account_id = $accountId
      ORDER BY created_at DESC
    """.query[Transaction].to[List]
