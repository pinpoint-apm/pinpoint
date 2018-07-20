package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointConfig("rabbitmq/client/pinpoint-rabbitmq.config")
@Dependency({"com.rabbitmq:amqp-client:[4.0.0,4.max)", "org.apache.qpid:qpid-broker:6.1.1"})
@JvmArgument({"-Dpinpoint.configcenter=false"})
public class RabbitMQClient_4_x_IT extends RabbitMQClientITBase {

    @Test
    public void testPush() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);

        testRunner.runPushTest();
    }

    @Test
    public void testPush_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);

        testRunner.runPushTest();
    }

    @Test
    public void testPush_nio() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.useNio();

        testRunner.runPushTest();
    }

    @Test
    public void testPush_nio_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.useNio();

        testRunner.runPushTest();
    }

    @Test
    public void testPull() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);

        testRunner.runPullTest();
    }

    @Test
    public void testPull_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);

        testRunner.runPullTest();
    }

    @Test
    public void testPull_nio() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.useNio();

        testRunner.runPullTest();
    }

    @Test
    public void testPull_nio_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.useNio();

        testRunner.runPullTest();
    }
}
