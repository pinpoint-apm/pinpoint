package com.navercorp.pinpoint.it.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.junit.jupiter.api.BeforeAll;
import test.pinpoint.plugin.rabbitmq.spring.config.CommonConfig;

import java.util.Properties;

/**
 * @author kootaejin
 */
public class SpringAmqpRabbitITBase {

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
        int port = Integer.parseInt(System.getProperty("PORT"));
        CommonConfig.setPort(port);
    }
}
