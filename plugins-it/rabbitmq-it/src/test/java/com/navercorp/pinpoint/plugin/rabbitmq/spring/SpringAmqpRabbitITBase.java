package com.navercorp.pinpoint.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;

import java.util.Properties;

/**
 * @author kootaejin
 */
public class SpringAmqpRabbitITBase {

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        // empty
    }

}
