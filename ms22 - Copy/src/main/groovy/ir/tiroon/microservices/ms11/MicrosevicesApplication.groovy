package ir.tiroon.microservices.ms11

import ir.tiroon.microservices.ms11.model.Event
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.scheduling.annotation.EnableAsync

@EnableKafka
@EnableAsync
@Configuration
@SpringBootApplication
class MicrosevicesApplication {

	static void main(String[] args) {
		SpringApplication.run MicrosevicesApplication, args
	}

	@Value('${kafka.bootstrap-servers}')
	private String bootstrapServers


	@Bean
	KafkaTemplate<String, Event> kafkaTemplate() {
		Map<String, Object> configProps = new HashMap<>()
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class)
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class)

		new KafkaTemplate<String, Event>(
				new DefaultKafkaProducerFactory<>(configProps)
		)
	}



//	@Bean(name = 'applicationEventMulticaster')
//	ApplicationEventMulticaster simpleApplicationEventMulticaster() {
//		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster()
//		eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor())
//		return eventMulticaster
//	}


//	@EventListener
//	def handleOrderCreatedEvent(event pEvent) throws JsonProcessingException {
//        PersonRegisteredEvent se = new PersonRegisteredEvent(pEvent)
//        kafkaTemplate().send('mytesttopic',se)
//	}

}
