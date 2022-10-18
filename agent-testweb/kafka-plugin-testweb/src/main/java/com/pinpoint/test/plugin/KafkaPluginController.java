package com.pinpoint.test.plugin;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class KafkaPluginController {
    public static final String TOPIC = "kafka-it-topic";
    public static final String MESSAGE = "message-" + UUID.randomUUID().toString();
    public static final String TRACE_TYPE_RECORD = "RECORD";

    @GetMapping("/")
    public String welcome() {
        return "WELCOME";
    }

    @GetMapping("/producer")
    public String producer() {
        TestProducer producer = new TestProducer();
        producer.sendMessage(KafkaPluginTestConstants.BROKER_URL, 1, KafkaPluginTestConstants.TRACE_TYPE_RECORD);

        return "OK";
    }

    @GetMapping("/consumer")
    public String consumer() {
        TestConsumer consumer = new TestConsumer(KafkaPluginTestConstants.BROKER_URL);
        consumer.start();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
        }

        return "OK";
    }
}
