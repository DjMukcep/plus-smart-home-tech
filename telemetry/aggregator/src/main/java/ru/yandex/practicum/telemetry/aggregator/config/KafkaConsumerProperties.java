package ru.yandex.practicum.telemetry.aggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.consumer")
public record KafkaConsumerProperties(String groupId, Boolean autoCommit) {
}
