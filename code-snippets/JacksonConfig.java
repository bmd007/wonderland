package com.github.bmd007.telegramtopiccleanerbot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
            .modules(new JavaTimeModule())
            .featuresToEnable(
                MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
                DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN,
                SerializationFeature.INDENT_OUTPUT
            )
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
