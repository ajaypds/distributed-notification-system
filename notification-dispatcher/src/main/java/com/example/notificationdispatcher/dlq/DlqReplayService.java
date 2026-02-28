package com.example.notificationdispatcher.dlq;

import com.example.contract.NotificationEvent;
import com.example.notificationdispatcher.kafka.KafkaRetryConstants;
import com.example.notificationdispatcher.metrics.DispatcherMetrics;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DlqReplayService {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final DispatcherMetrics metrics;

    public int replayAll(){
        KafkaConsumer<String, NotificationEvent> consumer = createDlqConsumer();

        consumer.subscribe(
                List.of(KafkaRetryConstants.DLQ_TOPIC)
        );

        ConsumerRecords<String, NotificationEvent> records =
                consumer.poll(Duration.ofSeconds(5));

        AtomicInteger replayed = new AtomicInteger();

        records.forEach(record -> {
            kafkaTemplate.send(
                    KafkaRetryConstants.MAIN_TOPIC,
                    record.key(),
                    record.value()
            );
            metrics.incrementDlqReplay();
            replayed.getAndIncrement();
        });

        consumer.close();
        return replayed.get();
    }

    private KafkaConsumer<String, NotificationEvent> createDlqConsumer(){

        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-replay-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JacksonJsonSerde<NotificationEvent> serde = new JacksonJsonSerde<>(NotificationEvent.class);

        return new KafkaConsumer<>(
                props,
                new StringDeserializer(),
                serde.deserializer()
        );

    }
}

