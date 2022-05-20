package statefull.geofencing.faas.location.aggregate.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@EnableKafkaStreams
@Configuration
public class KafkaStreamsDefaultConfig {

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfig(
            @Value("${spring.application.name}") String applicationName,
            @Value("${spring.kafka.bootstrap-servers}") String bootStrapServers,
            @Value("${kafka.streams.replication-factor:1}") int replicationFactor,
            @Value("${kafka.streams.properties.num.stream.threads:1}") int numStreamThreads,
            @Value("${kafka.streams.server.config.app.port:9585}") int port,
            @Value("${kafka.streams.server.config.app.ip:localhost}") String ip,
            Environment environment) {
        var props = new HashMap<String, Object>();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, replicationFactor);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, numStreamThreads);

        // Using this means accepting the app to continue when it faces a deserialization error, instead of break down
//        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);

        //        // This configuration is for making remote interactive queries possible
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, ip + ":" + port);

        var activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
        //The properties below, should not be applied when active profile is test
        if (!activeProfiles.contains("test") && !activeProfiles.contains("local")) {
            props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        } else {
            props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100); //to slow down commits so that tests can catch up with the eventual consistency ?>!
        }
        //TODO investigate usage of        cache.max.bytes.buffering
        //TODO investigate usage of         state.cleanup.delay.ms
        return new KafkaStreamsConfiguration(props);
    }

}
