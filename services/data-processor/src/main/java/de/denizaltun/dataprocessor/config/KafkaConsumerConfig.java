package de.denizaltun.dataprocessor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.denizaltun.dataprocessor.dto.VehicleTelemetryMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 * Kafka consumer configuration.
 * Sets up JSON deserialization for VehicleTelemetryMessage.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Configure ObjectMapper to handle Java 8 date/time types.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Configure Kafka consumer factory with JSON deserialization and SASL/SSL.
     */
    @Bean
    public ConsumerFactory<String, VehicleTelemetryMessage> consumerFactory() {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties(null);

        // Configure JsonDeserializer properties
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, VehicleTelemetryMessage.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(VehicleTelemetryMessage.class, objectMapper())
        );
    }

    /**
     * Configure Kafka listener container factory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VehicleTelemetryMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VehicleTelemetryMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}