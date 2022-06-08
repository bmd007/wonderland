package wonderland.wonder.matcher.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@EnableKafkaStreams
@Configuration
public class KafkaStreamsDefaultConfig {

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfig(
            @Value("${spring.application.name}") String applicationName,
            @Value("${spring.kafka.bootstrap-servers}") String bootStrapServers,
            @Value("${kafka.streams.server.config.app-ip}") String ip,
            @Value("${kafka.streams.server.config.app-port}") String port,
            @Value("${spring.kafka.streams.replication-factor}") int kafkaStreamsReplicationFactor,
            Environment environment) {
        Map<String, Object> props = new HashMap<>();
        // equivalent to group-wonderSeekerId
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationName);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, applicationName + "-" + UUID.randomUUID());

        // Using this means accepting the app to continue when it faces a deserialization error, instead of break down
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);

        // this is needed
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // This configuration is for making remote interactive queries possible
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, ip + ":" + port);
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, kafkaStreamsReplicationFactor);

        props.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/mohami/workspace/repositories/Wonderland/.kafkastreams");

        var activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
        //The properties below, should not be applied when active profile is test or mock
        //But if the active profiles include docker-compose-all, they should be applied regardless of other active profiles.
        if (!activeProfiles.contains("test")) {
            props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        } else {
            props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100); //to slow down commits so that tests can catch up with the eventual consistency ?>!
        }
//TODO investigate usage of        cache.max.bytes.buffering
//TODO investigate usage of         state.cleanup.delay.ms
        return new KafkaStreamsConfiguration(props);
    }

}
