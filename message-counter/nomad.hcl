job "message-counter" {
  region = "global"
  datacenters = [
    "dc1"]
  type = "service"

  update {
    # The update stanza specifies the group's update strategy.
    max_parallel = 1
    health_check = "checks"
    min_healthy_time = "30s"
  }

  group "message-counter" {
    #count = INSTANCE_COUNT
    count = 2

    restart {
      delay = "15s"
      mode = "delay"
    }

    task "message-counter" {
      driver = "docker"

      # Configuration is specific to each driver.
      config {
        image = "bmd007/message-counter:latest"
        force_pull = true
        auth {
          username = "bmd007"
          password = ""
        }

        port_map {
          http = 9585
        }
      }

      env {
        SPRING_PROFILES_ACTIVE = "nomad"
        SPRING_KAFKA_BOOTSTRAP_SERVERS = "kafka:9092"
        KAFKA_STREAMS_SERVER_CONFIG_APP_IP = "message-counter"
        KAFKA_STREAMS_SERVER_CONFIG_APP_PORT = "${NOMAD_PORT_http}"
        CONFIG_SERVER_IP = "http://config-center"
        CONFIG_SERVER_PORT = "8888"
        SPRING_CLOUD_CONSUL_HOST = "${NOMAD_IP_http}"
        #        SPRING_APPLICATION_INSTANCE_ID =                           "${NOMAD_ALLOC_ID}"
        JAVA_OPTS = "-XshowSettings:vm -XX:+ExitOnOutOfMemoryError -Xmx200m -Xms150m -XX:MaxDirectMemorySize=48m -XX:ReservedCodeCacheSize=64m -XX:MaxMetaspaceSize=128m -Xss256k"
      }
      resources {
        cpu = 256
        memory = 250
        network {
          mbits = 1
          port "http" {}
        }
      }
    }
  }
}
