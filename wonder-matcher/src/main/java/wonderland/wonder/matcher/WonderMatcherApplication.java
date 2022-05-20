package wonderland.wonder.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@SpringBootApplication(scanBasePackages = {"wonderland.wonder.matcher"})
public class WonderMatcherApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(WonderMatcherApplication.class);
    private org.h2.tools.Server webServer;
    private org.h2.tools.Server server;

    public static void main(String[] args) {
        SpringApplication.run(WonderMatcherApplication.class, args);
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
