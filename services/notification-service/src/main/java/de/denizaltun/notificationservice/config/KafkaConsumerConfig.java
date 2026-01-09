package de.denizaltun.notificationservice.config;

import de.denizaltun.notificationservice.dto.AlertEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, AlertEvent> consumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        // Add JsonDeserializer properties - these will be applied via configure()
        // Using constructor-only (no setters) keeps typeMapper null, avoiding Spring Kafka 3.x conflict
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AlertEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Create deserializer with target type only - don't call any setters!
        // The factory will call configure() which applies properties from the map
        JsonDeserializer<AlertEvent> deserializer = new JsonDeserializer<>(AlertEvent.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AlertEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AlertEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
