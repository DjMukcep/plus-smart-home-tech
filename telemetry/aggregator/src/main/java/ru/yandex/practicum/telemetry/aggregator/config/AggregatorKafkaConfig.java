package ru.yandex.practicum.telemetry.aggregator.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.aggregator.deserializer.BaseAvroDeserializer;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class AggregatorKafkaConfig {

    @Bean
    public Consumer<String, SensorEventAvro> avroConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "telemetry-aggregator-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props, new StringDeserializer(),
                new BaseAvroDeserializer<>(SensorEventAvro.getClassSchema()));
    }
}
