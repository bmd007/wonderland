package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.model.PersonRegisteredEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@EnableKafka
@Configuration
class kafkaConfig {

    @Value('${kafka.bootstrap-servers}')
    String bootstrapServers

    @Value('${kafka.group-id}')
    String groupId

    @Bean
    ConsumerFactory<String, PersonRegisteredEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class)

        def jsonDeserializer = new JsonDeserializer(PersonRegisteredEvent.class)
        jsonDeserializer.addTrustedPackages("ir.tiroon.microservices.model")

        new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),jsonDeserializer)
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PersonRegisteredEvent> kafkaListenerContainerFactory() {
        def factory = new ConcurrentKafkaListenerContainerFactory<String, PersonRegisteredEvent>()
        factory.setConsumerFactory(consumerFactory())

        factory
    }

    @KafkaListener(topics = "mytesttopic3")
    void listen(PersonRegisteredEvent pre) {
        System.out.println("BMD::Received Message : " + pre)
    }

}
