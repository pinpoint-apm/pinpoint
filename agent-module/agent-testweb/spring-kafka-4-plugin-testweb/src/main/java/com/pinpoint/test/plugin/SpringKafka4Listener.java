package com.pinpoint.test.plugin;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringKafka4Listener {

    @KafkaListener(id = "record", topics = SpringKafka4TestConstants.RECORD_TOPIC)
    public void listenRecord(ConsumerRecord<String, String> record) {
        System.out.println("Received record: " + record.value());
    }

    @KafkaListener(id = "batch-list", topics = SpringKafka4TestConstants.BATCH_LIST_TOPIC, batch = "true")
    public void listenBatchList(List<ConsumerRecord<String, String>> records) {
        System.out.println("Received batch list: " + records.size());
    }

    // The container passes an empty record list to doInvokeBatchListener for this listener type.
    @KafkaListener(id = "batch-records", topics = SpringKafka4TestConstants.BATCH_RECORDS_TOPIC, batch = "true")
    public void listenBatchRecords(ConsumerRecords<String, String> records) {
        System.out.println("Received batch records: " + records.count());
    }

    @KafkaListener(id = "batch-error", topics = SpringKafka4TestConstants.BATCH_ERROR_TOPIC, batch = "true")
    public void listenBatchError(List<ConsumerRecord<String, String>> records) {
        throw new IllegalStateException("batch listener failure, size=" + records.size());
    }
}
