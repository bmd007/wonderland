package statefull.geofencing.faas.location.aggregate;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@SpringBootApplication(scanBasePackages = {"statefull.geofencing.faas"})
public class LocationAggregateApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationAggregateApplication.class);
    private org.h2.tools.Server webServer;
    private org.h2.tools.Server server;

    public static void main(String[] args) {
        SpringApplication.run(LocationAggregateApplication.class, args);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
    public void setupH2Console() {
        try {
            this.webServer = org.h2.tools.Server.createWebServer("-webAllowOthers", "-webPort", "8084").start();
            this.server = org.h2.tools.Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9097").start();
        } catch (SQLException throwable) {
            LOGGER.error("", throwable);
        }
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {
        this.webServer.stop();
        this.server.stop();
    }
}
