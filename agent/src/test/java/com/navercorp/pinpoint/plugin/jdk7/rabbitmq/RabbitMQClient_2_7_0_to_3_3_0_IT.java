package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointConfig("rabbitmq/client/pinpoint-rabbitmq.config")
@Dependency({"com.rabbitmq:amqp-client:[2.7.0,3.0.0)", "org.apache.qpid:qpid-broker:6.1.1"})
@JvmArgument({"-Dpinpoint.configcenter=false"})
public class RabbitMQClient_2_7_0_to_3_3_0_IT extends RabbitMQClientITBase {

    @Test
    public void testPush() throws Exception {
        testRunner.runPushTest();
    }

    @Test
    public void testPull() throws Exception {
        testRunner.runPullTest();
    }
}
