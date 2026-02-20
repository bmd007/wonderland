package io.github.pubsubseekbucket.subscribe.integrationtest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import io.github.pubsubseekbucket.EventBusConfigurationNnx;

@Import({EventBusConfigurationNnx.class})
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
