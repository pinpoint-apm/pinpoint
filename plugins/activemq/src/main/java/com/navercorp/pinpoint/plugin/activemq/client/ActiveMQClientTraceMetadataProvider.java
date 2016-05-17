package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT, AnnotationKeyMatchers.exact(AnnotationKey.MESSAGE_QUEUE_URI));
        context.addServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL, AnnotationKeyMatchers.ARGS_MATCHER);

        context.addAnnotationKey(ActiveMQClientConstants.ACTIVEMQ_BROKER_URL);
        context.addAnnotationKey(ActiveMQClientConstants.ACTIVEMQ_MESSAGE);
    }

}