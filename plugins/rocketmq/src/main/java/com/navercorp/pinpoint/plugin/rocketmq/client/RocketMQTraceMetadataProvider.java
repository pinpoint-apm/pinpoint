package com.navercorp.pinpoint.plugin.rocketmq.client;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 */
public class RocketMQTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(RocketMQConstants.ROCKETMQ_CLIENT, AnnotationKeyMatchers.exact(AnnotationKey.MESSAGE_QUEUE_URI));
        
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_BROKER_URL);
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_MESSAGE);
    }
}
