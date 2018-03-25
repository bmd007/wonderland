package ir.tiroon.microservices.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import ir.tiroon.microservices.model.PersonInterest
import ir.tiroon.microservices.model.PersonInterestAddedEvent
import ir.tiroon.microservices.repository.PersonInterestRepository
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import reactor.core.publisher.Mono

@EnableKafka
@Configuration
class kafkaConfig {

    @Value('${kafka.bootstrap-servers}')
    String bootstrapServers

    @Value('${kafka.group-id}')
    String groupId

    @Bean
    ConsumerFactory<String, PersonInterestAddedEvent> consumerFactory(ObjectMapper om) {
        Map<String, Object> props = new HashMap<>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class)


        JsonDeserializer jsonDeserializer = new JsonDeserializer(PersonInterestAddedEvent.class, om)
        jsonDeserializer.addTrustedPackages("ir.tiroon.microservices.model")

        new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer)
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PersonInterestAddedEvent> kafkaListenerContainerFactory() {
        def factory = new ConcurrentKafkaListenerContainerFactory<String, PersonInterestAddedEvent>()
        factory.setConsumerFactory(consumerFactory())

        factory
    }

    @Autowired
    PersonInterestRepository personInterestRepo

    @KafkaListener(topics = "mytesttopic7")
    void listen(PersonInterestAddedEvent piae) {

        System.out.println("BMD::Received Message : " + piae.key.phoneNumber + ';;;' + piae.key.localDateTime)


        PersonInterest interest = personInterestRepo.findByPhoneNumber(piae.key.phoneNumber)
                .blockOptional().orElse(new PersonInterest(piae.key.phoneNumber))

        interest.addInterest(piae.interestName)

        personInterestRepo.save(interest).subscribe()
        
    }
}