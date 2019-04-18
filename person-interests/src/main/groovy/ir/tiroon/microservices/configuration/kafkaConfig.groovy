package ir.tiroon.microservices.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import ir.tiroon.microservices.event.command.AddInterestCommand
import ir.tiroon.microservices.model.PersonInterests
import ir.tiroon.microservices.repository.PersonInterestRepository
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

@EnableKafka
@Configuration
class kafkaConfig {

    @Value('${kafka.bootstrap-servers}')
    String bootstrapServers

    @Value('${kafka.group-id}')
    String groupId

//    @Bean
//    ConsumerFactory<String, AddInterestCommand> consumerFactory(ObjectMapper om) {
//        Map<String, Object> props = new HashMap<>()
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class)
//
//        JsonDeserializer jsonDeserializer = new JsonDeserializer(PersonInterestAddedEvent.class, om)
//        jsonDeserializer.addTrustedPackages("ir.tiroon.microservices.event")
//
//        new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer)
//    }
//
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<String, PersonInterestAddedEvent> kafkaListenerContainerFactory() {
//        def factory = new ConcurrentKafkaListenerContainerFactory<String, PersonInterestAddedEvent>()
//        factory.setConsumerFactory(consumerFactory())
//
//        factory
//    }

    private static final Logger LOGGER = LoggerFactory.getLogger(this.class);

    @Bean
    KafkaTemplate kafkaTemplate(ObjectMapper om) {
        Map<String, Object> configProps = new HashMap<>()
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class)

        new KafkaTemplate(
                new DefaultKafkaProducerFactory<>(configProps,
                        new StringSerializer(), new JsonSerializer(om))
        )
    }

    @Autowired
    PersonInterestRepository personInterestRepo

    //TODO use a producer(or turn to spring-stream-processor) and sent an event as
    // a result of work notification
    @KafkaListener(topics = "add-interest-command")
    void listen(AddInterestCommand command) {

        PersonInterests interest = personInterestRepo
                .findById(command.email)
                .defaultIfEmpty(new PersonInterests(command.email))
                .map{
                    interests -> interests.interests.add(command.interest)
                    return interests
                }
                .flatMap() { interests ->
                    personInterestRepo.save(interests)
                }
                .doOnNext{ interests -> //TODO use this one instead of command
                    LOGGER.info("Interest {} added to {}", command.interest, command.email)
                }
    }
}