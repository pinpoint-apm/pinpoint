package com.pinpoint.test.plugin;

import java.util.UUID;

public class KafkaPluginTestConstants {
    // override with -Dkafka.broker.url=host:port
    public static final String BROKER_URL = System.getProperty("kafka.broker.url", "localhost:9092");
    public static final String TOPIC = "kafka-it-topic";
    public static final String MESSAGE = "message-" + UUID.randomUUID().toString();
    public static final String GROUP_ID = "kafka-it-group";
    public static final String KAFKA_CLIENT_SERVICE_TYPE = "KAFKA_CLIENT";
    public static final String KAFKA_CLIENT_INTERNAL_SERVICE_TYPE = "KAFKA_CLIENT_INTERNAL";
    public static final int PARTITION = 0;
    public static final String TRACE_TYPE_RECORD = "RECORD";
    public static final String TRACE_TYPE_MULTI_RECORDS = "MULTI_RECORDS";
    public static final long MAX_TRACE_WAIT_TIME = 10000;
}
