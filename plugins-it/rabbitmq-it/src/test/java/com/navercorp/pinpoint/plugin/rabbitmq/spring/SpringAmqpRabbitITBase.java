package com.navercorp.pinpoint.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.plugin.rabbitmq.util.TestBroker;
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;

/**
 * @author kootaejin
 */
public class SpringAmqpRabbitITBase {

    private static final TestBroker BROKER = new TestBroker();

    @BeforeSharedClass
    public static void sharedSetUp() throws Exception {
        BROKER.start();
    }

    @AfterSharedClass
    public static void sharedTearDown() {
        BROKER.shutdown();
    }

}
