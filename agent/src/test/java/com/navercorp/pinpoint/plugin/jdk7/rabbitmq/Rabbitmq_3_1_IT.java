package com.navercorp.pinpoint.plugin.jdk7.rabbitmq;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.runner.RunWith;

/**
 * @author Jiaqi Feng
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.rabbitmq:amqp-client:[3.0.0,3.2.0)", "org.apache.qpid:qpid-broker:6.1.1"})
@JvmArgument({"-Dpinpoint.configcenter=false"})
public class Rabbitmq_3_1_IT extends RabbitmqIT {
    public Rabbitmq_3_1_IT() {
        super();
        RUNNABLE_NAME="ConsumerDispatcher$4";
    }
}
