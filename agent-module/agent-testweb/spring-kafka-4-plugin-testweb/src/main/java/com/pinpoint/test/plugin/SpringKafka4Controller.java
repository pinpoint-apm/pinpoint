package com.pinpoint.test.plugin;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringKafka4Controller {

    private static final int BATCH_SIZE = 3;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public SpringKafka4Controller(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/")
    public String welcome() {
        return "/producer/record, /producer/batch-list, /producer/batch-records, /producer/batch-error";
    }

    @GetMapping("/producer/record")
    public String producerRecord() {
        kafkaTemplate.send(SpringKafka4TestConstants.RECORD_TOPIC, "Hello, World!");
        return "OK";
    }

    @GetMapping("/producer/batch-list")
    public String producerBatchList() {
        return sendBatch(SpringKafka4TestConstants.BATCH_LIST_TOPIC);
    }

    @GetMapping("/producer/batch-records")
    public String producerBatchRecords() {
        return sendBatch(SpringKafka4TestConstants.BATCH_RECORDS_TOPIC);
    }

    @GetMapping("/producer/batch-error")
    public String producerBatchError() {
        return sendBatch(SpringKafka4TestConstants.BATCH_ERROR_TOPIC);
    }

    private String sendBatch(String topic) {
        for (int i = 0; i < BATCH_SIZE; i++) {
            kafkaTemplate.send(topic, "Hello, Batch! " + i);
        }
        return "OK";
    }
}
