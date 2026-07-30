package ru.yandex.practicum.telemetry.collector.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class KafkaProducerCloser {

    private final Producer<String, SpecificRecordBase> producer;

    @PreDestroy
    public void shutdown() {
        producer.flush();
        producer.close(Duration.ofSeconds(5));
    }
}
