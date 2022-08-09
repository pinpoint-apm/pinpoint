package com.navercorp.pinpoint.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.CommonConfig;

import java.util.Properties;

/**
 * @author kootaejin
 */
public class SpringAmqpRabbitITBase {

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        // empty

        int port = Integer.parseInt(beforeAllResult.getProperty("PORT"));
        CommonConfig.setPort(port);
    }
}
