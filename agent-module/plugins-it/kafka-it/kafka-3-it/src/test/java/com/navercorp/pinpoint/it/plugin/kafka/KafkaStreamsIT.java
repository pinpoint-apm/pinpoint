package com.navercorp.pinpoint.it.plugin.kafka;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;

import java.util.Properties;

public abstract class KafkaStreamsIT {

    protected static final Logger logger = LogManager.getLogger(KafkaStreamsIT.class);

    static String brokerUrl;
    static int PORT;


    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
        PORT = Integer.parseInt(System.getProperty("PORT"));
        brokerUrl = "localhost:" + PORT;
    }

    public static int getPort() {
        return PORT;
    }

}
