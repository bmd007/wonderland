job "service_registry" {
  region =      "global"
  datacenters = ["dc1"]
  type =        "batch"

  parameterized {
    meta_required = ["DOCKER_HUB_PASSOWRD"]
  }

 group "service_registry" {
    #count = INSTANCE_COUNT
    count = 1

    restart {
      delay = "15s"
      mode =  "delay"
    }

    task "service_registry" {
      driver = "docker"
      config {
        network_mode = "host"
        #hostname = "service_registry"
        image =      "bmd007/service_registry:latest"
        force_pull = true
        auth {
          username = "bmd007"
          password = "${NOMAD_META_DOCKER_HUB_PASSOWRD}"
        }

        port_map {
          http =       8761
        }
      }

      env {
        SPRING_PROFILES_ACTIVE =                                  "nomad"
        EUREKA_INSTANCE_HOSTNAME =                                "10.71.216.152"
        INSTANCE_NOMAD_PORT =                                     "8761"
        CONFIG_SERVER_IP =                                        "http://10.71.216.152"
        CONFIG_SERVER_PORT =                                      "8888"
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
	        static = 8761
            to     = 8761
	      }
        }
      }
    }
  }
}
