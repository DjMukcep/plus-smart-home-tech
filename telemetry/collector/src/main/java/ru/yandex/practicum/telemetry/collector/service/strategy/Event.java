package ru.yandex.practicum.telemetry.collector.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

@RequiredArgsConstructor
@Slf4j
public abstract class Event {

    private final Producer<String, SpecificRecordBase> producer;

    protected void sendMessage(String topic, SpecificRecordBase message) {
        producer.send(new ProducerRecord<>(topic, message), this::onMessageSent);
    }

    private void onMessageSent(RecordMetadata metadata, Exception ex) {
        if (ex != null) {
            log.error("Ошибка отправки сообщения в топик {}", metadata != null ? metadata.topic() : "unknown", ex);
        } else  {
            log.info("Сообщение отправлено. topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());
        }
    }
}
