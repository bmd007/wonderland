package ir.tiroon.microservices.ms11.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.expression.common.LiteralExpression
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.messaging.MessageHandler

//@Configuration
class ProducingChannelConfig {

    @Value('${kafka.bootstrap-servers}')
    private String bootstrapServers;


    @Bean
    DirectChannel ProducingChannel(){
       return new DirectChannel()
    }


    @Bean
    @ServiceActivator(inputChannel = 'producingChannel')
    MessageHandler kafkaMessageHandler() {
        KafkaProducerMessageHandler<String, String> handler =
                new KafkaProducerMessageHandler<>(kafkaTemplate())
        handler.setMessageKeyExpression(new LiteralExpression('kafka-integration'))

        return handler;
    }

    @Bean
     KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory())
    }

    @Bean
     ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
     Map<String, Object> producerConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // introduce a delay on the send to allow more messages to accumulate
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);

        return properties;
    }
}