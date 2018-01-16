package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.SaslConfig;
import com.rabbitmq.client.SaslMechanism;
import com.rabbitmq.client.impl.LongStringHelper;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static org.junit.Assert.assertTrue;

/**
 * @author Jiaqi Feng
 */

public class RabbitmqIT {
    public static String publishClassSignature="com.rabbitmq.client.impl.ChannelN.basicPublish(java.lang.String, java.lang.String, boolean, boolean, com.rabbitmq.client.AMQP$BasicProperties, byte[])";

    public static String RUNNABLE_NAME="ConsumerDispatcher$5";
    public static Broker broker;

    public RabbitmqIT() {}

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Broker broker = new Broker();
        BrokerOptions brokerOptions = new BrokerOptions();
        brokerOptions.setConfigProperty("qpid.amqp_port", "20179");
        brokerOptions.setConfigurationStoreType("Memory");
        System.setProperty("qpid.work_dir", "/tmp/qpidworktmp");
        System.setProperty("qpid.initialConfigurationLocation", "qpid/qpid-config.json");
        brokerOptions.setStartupLoggedToSystemOut(false);

        //System.out.println("--------------------- start qpid option="+brokerOptions.toString());
        try {
            broker.startup(brokerOptions);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("--------------------- start qpid failed ");
            return;
        }
        //System.out.println("--------------------- start qpid successed ");
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (broker!=null) {
            //System.out.println("--------------------- shutdown qpid successed ");
            broker.shutdown();
            broker=null;
        }
    }

    @Test
    public void testRabbitmq() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        String exchange="test-pp";
        String queuename="queue-pp";
        String message="hello rabbit mq";

        SaslConfig saslConfig = new SaslConfig() {
            public SaslMechanism getSaslMechanism(String[] mechanisms) {
                return new SaslMechanism() {
                    public String getName() {
                        return "ANONYMOUS";
                    }

                    public LongString handleChallenge(LongString challenge, String username, String password) {
                        return LongStringHelper.asLongString("");
                    }
                };
            }
        };
        // producer side
        //System.out.println("RabbitmqSender.doGet() ---- start");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(20179);
            factory.setSaslConfig(saslConfig);

            // enable below to test AutorecoveringChannel
            //factory.setAutomaticRecoveryEnabled(true);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "direct", false);
            channel.queueDeclare(queuename, false, false, false, null);
            channel.queueBind(queuename, exchange, "test");

            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            channel.basicPublish(exchange, "test", false, false, builder.appId("test").build(), message.getBytes());
            //System.out.println("RabbitmqSender ---- Sent message:" + message);

            channel.close();
            connection.close();

            //comsumer
            factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(20179);
            factory.setSaslConfig(saslConfig);
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(queuename, false, false, false, null);
            //System.out.println("Receiver ---- Waiting for messages");

            final CountDownLatch latch = new CountDownLatch(1);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String message = new String(body, "UTF-8");
                    //System.out.println("Receiver ---- Received exchange=" + envelope.getExchange() + ", routingkey=" + envelope.getRoutingKey() + ", message=" + message);
                    latch.countDown();
                }
            };
            channel.basicConsume(queuename, true, consumer);

            // wait consumer
            assertTrue(latch.await(10, TimeUnit.SECONDS));

        } catch (Exception e) {
            e.printStackTrace();
        }

        verifier.printCache();
        verifier.verifyTrace(Expectations.event("RABBITMQ", publishClassSignature,
                        annotation("rabbitmq.exchange", "test-pp"),
                        annotation("rabbitmq.routingkey", "test")),
                Expectations.root("RABBITMQ", "com.rabbitmq.client.DefaultConsumer.handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP$BasicProperties, byte[])", null, "exchange:test-pp", "127.0.0.1:20179",
                        annotation("rabbitmq.routingkey", "test")));

        verifier.verifyTraceCount(0);
    }
}
