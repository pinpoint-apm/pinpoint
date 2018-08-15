package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.plugin.jdk7.rabbitmq.util.RabbitMQTestConstants;
import com.navercorp.pinpoint.plugin.jdk7.rabbitmq.util.TestBroker;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */

public abstract class RabbitMQClientITBase {

    private static final TestBroker BROKER = new TestBroker();

    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    protected final RabbitMQTestRunner testRunner = new RabbitMQTestRunner(connectionFactory);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BROKER.start();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        BROKER.shutdown();
    }

    @Before
    public void setUp() {
        connectionFactory.setHost(RabbitMQTestConstants.BROKER_HOST);
        connectionFactory.setPort(RabbitMQTestConstants.BROKER_PORT);
        connectionFactory.setSaslConfig(RabbitMQTestConstants.SASL_CONFIG);
    }

    final ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
