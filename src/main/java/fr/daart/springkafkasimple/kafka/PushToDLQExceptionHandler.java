package fr.daart.springkafkasimple.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PushToDLQExceptionHandler implements DeserializationExceptionHandler {

    public static final String DLQ_TOPIC_CONFIG = "deserialization.exception.handler.dlq.topic";
    private KafkaProducer<byte[], byte[]> kafkaProducer;
    private String dlqTopic;

    @Override
    public DeserializationHandlerResponse handle(ProcessorContext processorContext, ConsumerRecord<byte[], byte[]> consumerRecord, Exception exception) {
        try {
            kafkaProducer.send(computeRecord(consumerRecord, exception)).get();
            return DeserializationHandlerResponse.CONTINUE;
        } catch (Exception dlqProducerException) {
            log.error("An error occured while producing to DLQ", dlqProducerException);
            return DeserializationHandlerResponse.FAIL;
        }
    }

    private <K,V> ProducerRecord<K,V> computeRecord(
            ConsumerRecord<K,V> consumerRecord,
            Exception exception
    ) {

        RecordHeaders recordHeaders = new RecordHeaders(consumerRecord.headers().toArray());
        // we can add information to kafka headers such as stacktrace, time, etc...
        recordHeaders.add(new RecordHeader("HEY", "OH".getBytes(StandardCharsets.UTF_8)));
        recordHeaders.add(new RecordHeader("our-stack-trace", exception.getClass().getName().getBytes(StandardCharsets.UTF_8)));

        return new ProducerRecord<>(dlqTopic, consumerRecord.partition(), consumerRecord.key(), consumerRecord.value(), recordHeaders);

    }

    @Override
    public void configure(Map<String, ?> configMap) {

        Map<String, Object> properties = new HashMap<>(configMap);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        kafkaProducer = new KafkaProducer<>(properties);
        dlqTopic = (String) configMap.get(DLQ_TOPIC_CONFIG);
    }
}
