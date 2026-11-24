package com.pinpoint.test.plugin;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Prints the trace headers of the records it receives, so that the propagation from the producer can be checked
 * from the app output without a collector.
 */
@Component
public class SpringKafka4Listener {

    private static final String PINPOINT_HEADER_PREFIX = "Pinpoint-";

    @KafkaListener(id = "record", topics = SpringKafka4TestConstants.RECORD_TOPIC)
    public void listenRecord(ConsumerRecord<String, String> record) {
        System.out.println("Received record: " + record.value() + ", headers=" + pinpointHeaders(record));
    }

    @KafkaListener(id = "batch-list", topics = SpringKafka4TestConstants.BATCH_LIST_TOPIC, batch = "true")
    public void listenBatchList(List<ConsumerRecord<String, String>> records) {
        System.out.println("Received batch list: " + records.size());
        for (ConsumerRecord<String, String> record : records) {
            System.out.println("  " + record.value() + ", headers=" + pinpointHeaders(record));
        }
    }

    // The container passes an empty record list to doInvokeBatchListener for this listener type.
    @KafkaListener(id = "batch-records", topics = SpringKafka4TestConstants.BATCH_RECORDS_TOPIC, batch = "true")
    public void listenBatchRecords(ConsumerRecords<String, String> records) {
        System.out.println("Received batch records: " + records.count());
        for (ConsumerRecord<String, String> record : records) {
            System.out.println("  " + record.value() + ", headers=" + pinpointHeaders(record));
        }
    }

    @KafkaListener(id = "batch-error", topics = SpringKafka4TestConstants.BATCH_ERROR_TOPIC, batch = "true")
    public void listenBatchError(List<ConsumerRecord<String, String>> records) {
        throw new IllegalStateException("batch listener failure, size=" + records.size());
    }

    private static Map<String, String> pinpointHeaders(ConsumerRecord<String, String> record) {
        Map<String, String> headers = new TreeMap<>();
        for (Header header : record.headers()) {
            if (header.key().startsWith(PINPOINT_HEADER_PREFIX)) {
                headers.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
            }
        }
        return headers;
    }
}
