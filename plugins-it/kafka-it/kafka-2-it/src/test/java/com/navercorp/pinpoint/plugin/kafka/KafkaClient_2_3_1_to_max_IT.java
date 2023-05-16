package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import test.pinpoint.plugin.kafka.Kafka2UnitServer;
import test.pinpoint.plugin.kafka.TestProducer;

import java.util.Random;

import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;


/**
 * @author Younsung Hwang
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-kafka-client.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-kafka-plugin"})
@Dependency({
        "org.apache.kafka:kafka_2.12:[2.3.1]", "log4j:log4j:[1.2.17]", "commons-io:commons-io:[2.5.0]",
        "org.apache.kafka:kafka-clients:[2.3.1,2.3.max]"
})
@JvmVersion(8)
@SharedTestLifeCycleClass(Kafka2UnitServer.class)
public class KafkaClient_2_3_1_to_max_IT extends KafkaClient2ITBase {
    @Test
    public void producerSendTest() throws NoSuchMethodException {
        int messageCount = new Random().nextInt(5) + 1;
        final TestProducer producer = new TestProducer();
        producer.sendMessage(brokerUrl, messageCount);
        KafkaClientITBase.verifyProducerSend(brokerUrl, messageCount);
    }

    @Ignore
    @Test
    public void recordEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer();
        producer.sendMessage(brokerUrl, 1, TRACE_TYPE_RECORD);
        KafkaClientITBase.verifySingleConsumerEntryPoint(brokerUrl, offset);
    }

    @Test
    public void recordMultiEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer();
        producer.sendMessage(brokerUrl, 1, TRACE_TYPE_MULTI_RECORDS);
        KafkaClientITBase.verifyMultiConsumerEntryPoint(brokerUrl);
    }

}
