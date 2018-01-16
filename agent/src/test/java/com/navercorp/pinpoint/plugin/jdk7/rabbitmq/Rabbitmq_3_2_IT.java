package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.runner.RunWith;

/**
 * @author Jiaqi Feng
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.rabbitmq:amqp-client:[3.2.0,4.0.0)", "org.apache.qpid:qpid-broker:6.1.1"})
//@Dependency({"com.rabbitmq:amqp-client:3.5.5", "org.apache.qpid:qpid-broker:6.1.1"})
@JvmArgument({"-Dpinpoint.configcenter=false"})
public class Rabbitmq_3_2_IT extends RabbitmqIT {
    public Rabbitmq_3_2_IT() {
        super();
        RUNNABLE_NAME="ConsumerDispatcher$5";
    }
}
