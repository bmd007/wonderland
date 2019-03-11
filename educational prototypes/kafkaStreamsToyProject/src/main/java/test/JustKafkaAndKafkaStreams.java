package test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.internals.QueryableStoreProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableKafkaStreams
@EnableKafka
@SpringBootApplication
public class JustKafkaAndKafkaStreams {

    /*
     * this is a hello world project for using kafka and kafka streams through spring enablers
     * First we use kafkaTemplate to produce/publish some events/messages to kafka
     * Then we use KafkaStreams to consume those events and publish to another topic after some changes.
     * Then again we use KafkaStreams to consume changes and do some groupBy and Count operations (KTable)
     *
     * in case of having replicationFactor more than 1, the state of counter will be saved on different instances of kafka. So to get the real number of counter
     * we should query all of the instances. For that we will use ...??? We can use spring-cloud-streams
     *
     * application.properties includes configuration for kafkaTemplate (normal kafka client wrapped by spring trough a template)
     * kafkaStream configs are provided as @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
     *
     * kafkaTemplate is easy to work with regarding serialize and deserialization
     * KafkaStreams need their own SerDe to be defined when consuming and when producing (I made my own serializer/deserializer using Jackson)
     *
     * It is also possible to bring Spring Integration, or Spring cloud streams (which is built on top of spring integration) As another level(s) of abstraction.
     * So it will become easy to combine messaging with kafka. So we can easily consume kafka, process data and
     * produce into rabbitMQ, as an example;
     *
     * Dependencies: for the first part "    implementation 'org.springframework.kafka:spring-kafka' is enough
     * its also enough to use KafkaStreams but is relaying on 'org.apache.kafka:kafka-streams' as an optional dependency.
     * So for kafka streams 'org.apache.kafka:kafka-streams' should also be provided.
     * */
    public static void main(String[] args) {
        SpringApplication.run(JustKafkaAndKafkaStreams.class, args);
    }

    public static final String pageViewEventTopicForSimpleKafkaListener = "simple";
    public static final String pageViewEventTopicForAndKafkaStreamsAlone = "streams-alone";
    public static final String pageViewCountTopicWithKafkaStreamsAlone = "count";
    public static final String pageViewPageAsKeyTopicWithKafkaStreamsAlone = "pageAsKey";
    public static final String pageViewCountMaterializedViewName = "counterView";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kStreamsConfigs(@Value("${spring.kafka.bootstrap-servers}") String bootStrapServers) {
        Map<String, String> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testStreams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new MySerde().getClass().getName());
        return new StreamsConfig(props);
    }

    @Component
    public static class PageViewEvenSource implements ApplicationRunner {

        private final KafkaTemplate kafkaTemplate;

        public PageViewEvenSource(KafkaTemplate kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @Override
        public void run(ApplicationArguments args) {

            List<String> names = Arrays.asList("bmd", "abodi", "mashto", "havij", "hooooo");
            List<String> pages = Arrays.asList("blog", "siteMap", "start", "home", "login");

            Runnable runnable = () -> {
                String rPage = pages.get(new Random().nextInt(pages.size()));
                String rName = names.get(new Random().nextInt(names.size()));

                var pageViewEvent = new PageViewEvent(rName, rPage, Math.random() > 0.5 ? 10 : 1000);
                //Something is wrong with this version of send in terms os serialize/deserialization
                kafkaTemplate.send(pageViewEventTopicForSimpleKafkaListener, pageViewEvent);

                var record = new ProducerRecord<>(pageViewEventTopicForAndKafkaStreamsAlone, UUID.randomUUID().toString(), pageViewEvent);
                kafkaTemplate.send(record);
            };

            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
        }
    }

    @Bean
    KStream kafkaStream(StreamsBuilder streamsBuilder) {
        KStream<String, PageViewEvent> stream = streamsBuilder
                .stream(pageViewEventTopicForAndKafkaStreamsAlone, Consumed.with(Serdes.String(), new MySerde()));
        stream
                .map((k, v) -> new KeyValue<>(v.getPage(), "000"))
                .to(pageViewPageAsKeyTopicWithKafkaStreamsAlone, Produced.with(Serdes.String(), Serdes.String()));

        return stream;
    }

    @Bean
    KStream kafkaStream2(StreamsBuilder streamsBuilder) {
        KStream stream = streamsBuilder
                .stream(pageViewPageAsKeyTopicWithKafkaStreamsAlone, Consumed.with(Serdes.String(), Serdes.String()));
        stream
                .groupByKey()
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(pageViewCountMaterializedViewName))
                .toStream()
                .foreach((key, value) -> System.out.println("kstream counter : " + key + ":;'+'" + value));
        return stream;
    }

    @KafkaListener(topics = pageViewEventTopicForSimpleKafkaListener)
    void processMessage(PageViewEvent event) {
        System.out.println("Arrived as PageViewEvent " + event.getUserId());
    }

    @RestController
    public static class restControllerForPageViewCounter {


    }

    //second listener on this topic
//        @KafkaListener(topics = pageViewEventTopicForAndKafkaStreamsAlone)
//        void processMessage(ConsumerRecord<String, PageViewEvent> record) throws IOException {
//            System.out.println("Arrived as consumer record" + record.value().getPage());
//        }

}