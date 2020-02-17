job "config_center" {
  region =      "global"
  datacenters = ["dc1"]
#  type =        "service"
  type =        "batch"

#  update {
    # The update stanza specifies the group's update strategy.
#    max_parallel =     1
#    health_check =     "checks"
#    min_healthy_time = "30s"
#  }


  parameterized {
    meta_required = ["DOCKER_HUB_PASSOWRD"]
  }

 group "config_center" {
    #count = INSTANCE_COUNT
    count = 1

    restart {
      delay = "15s"
      mode =  "delay"
    }

    task "config_center" {
      driver = "docker"

      # Configuration is specific to each driver.
      config {
        network_mode = "host"
//        hostname = "config_center"
        image =      "bmd007/config_center:latest"
        force_pull = true
        auth {
          username = "bmd007"
          password = "${NOMAD_META_DOCKER_HUB_PASSOWRD}"
        }

        port_map {
          http =  8888
        }
      }

      env {
        SPRING_PROFILES_ACTIVE =                                  "nomad"
        SPRING_APPLICATION_INSTANCE_ID =                          "${NOMAD_ALLOC_ID}"
        JAVA_OPTS =                                               "-XshowSettings:vm -XX:+ExitOnOutOfMemoryError -Xmx200m -Xms150m -XX:MaxDirectMemorySize=48m -XX:ReservedCodeCacheSize=64m -XX:MaxMetaspaceSize=128m -Xss256k"
      }
      resources {
        cpu =    256
        memory = 250
        network {
          mode = "host"
          mbits = 1
          port "http" {
	        static = 8888
            to     = 8888
	      }
        }
      }
    }
  }
}
