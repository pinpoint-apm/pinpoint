package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.runner.RunWith;

/**
 * @author Jiaqi Feng
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.rabbitmq:amqp-client:[4.0.0,4.max)",
        "org.apache.qpid:qpid-broker:6.1.1"})
@JvmArgument({"-Dpinpoint.configcenter=false"})
public class Rabbitmq_4_x_IT extends RabbitmqIT {
    public Rabbitmq_4_x_IT() {
        super();
        publishClassSignature="com.rabbitmq.client.impl.recovery.AutorecoveringChannel.basicPublish(java.lang.String, java.lang.String, boolean, boolean, com.rabbitmq.client.AMQP$BasicProperties, byte[])";
    }
}
