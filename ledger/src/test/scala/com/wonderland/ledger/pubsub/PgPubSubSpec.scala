package com.wonderland.ledger.pubsub

import cats.effect.IO
import com.wonderland.ledger.PostgresSpec
import com.wonderland.ledger.db.Database
import scala.concurrent.duration.*

class PgPubSubSpec extends PostgresSpec:
  test("listen and receive notifications") {
    withContainers { container =>
      val config = databaseConfig(container)
      Database.migrate[IO](config) >>
        PgPubSub.resource[IO](config).use { pubSub =>
          for
            _ <- pubSub.listen("test_channel")
            _ <- pubSub.notify("test_channel", """{"event": "test"}""")
            received <- pubSub.stream.take(1).compile.toList.timeout(5.seconds)
          yield
            assertEquals(received.size, 1)
            assertEquals(received.head.channel, "test_channel")
            assert(received.head.payload.contains("test"))
        }
    }
  }
