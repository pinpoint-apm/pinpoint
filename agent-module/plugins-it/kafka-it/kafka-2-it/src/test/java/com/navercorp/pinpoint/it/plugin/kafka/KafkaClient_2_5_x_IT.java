package com.navercorp.pinpoint.it.plugin.kafka;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.kafka.Kafka3UnitServer;
import test.pinpoint.plugin.kafka.TestProducer;

import java.util.Random;

import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;


/**
 * @author Younsung Hwang
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-kafka-client.config")
@Dependency({
        "org.apache.kafka:kafka_2.12:[2.5.0]",
        "org.apache.kafka:kafka-clients:[2.5.0,2.5.max]"
})
@SharedDependency({
        TestcontainersOption.TEST_CONTAINER,
        TestcontainersOption.KAFKA,
        "org.apache.kafka:kafka_2.12:2.6.0"
})
@SharedTestLifeCycleClass(Kafka3UnitServer.class)
public class KafkaClient_2_5_x_IT extends KafkaClient2ITBase {
    Random random = new Random();
    @Test
    public void producerSendTest() throws NoSuchMethodException {
        int messageCount = random.nextInt(5) + 1;
        final TestProducer producer = new TestProducer(brokerUrl);
        producer.sendMessage(messageCount);
        KafkaClientITBase.verifyProducerSend(brokerUrl, messageCount);
    }

    @Disabled
    @Test
    public void recordEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);
        producer.sendMessage(1, TRACE_TYPE_RECORD);
        KafkaClientITBase.verifySingleConsumerEntryPoint(brokerUrl, offset);
    }

    @Test
    public void recordMultiEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);
        producer.sendMessage(1, TRACE_TYPE_MULTI_RECORDS);
        KafkaClientITBase.verifyMultiConsumerEntryPoint(brokerUrl);
    }

}
