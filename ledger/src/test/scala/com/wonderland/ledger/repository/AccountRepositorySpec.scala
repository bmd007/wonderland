package com.wonderland.ledger.repository

import cats.effect.IO
import com.wonderland.ledger.PostgresSpec
import com.wonderland.ledger.db.Database
import com.wonderland.ledger.domain.*
import doobie.implicits.*

class AccountRepositorySpec extends PostgresSpec:
  private val repo = AccountRepository()

  test("create and find account") {
    withContainers { container =>
      val xa = transactorFor(container)
      for
        _ <- Database.migrate[IO](databaseConfig(container))
        created <- repo.create(CreateAccount("Checking", "USD")).transact(xa)
        found <- repo.findById(created.id).transact(xa)
      yield
        assert(found.isDefined)
        assertEquals(found.get.name, "Checking")
        assertEquals(found.get.currency, "USD")
    }
  }

  test("update balance") {
    withContainers { container =>
      val xa = transactorFor(container)
      for
        _ <- Database.migrate[IO](databaseConfig(container))
        account <- repo.create(CreateAccount("Savings", "EUR")).transact(xa)
        _ <- repo.updateBalance(account.id, BigDecimal("500.00")).transact(xa)
        updated <- repo.findById(account.id).transact(xa)
      yield
        assert(updated.get.balance.compare(BigDecimal("500.00")) == 0)
    }
  }

  test("find all accounts") {
    withContainers { container =>
      val xa = transactorFor(container)
      for
        _ <- Database.migrate[IO](databaseConfig(container))
        _ <- repo.create(CreateAccount("A1", "USD")).transact(xa)
        _ <- repo.create(CreateAccount("A2", "EUR")).transact(xa)
        all <- repo.findAll.transact(xa)
      yield
        assert(all.size >= 2)
    }
  }
