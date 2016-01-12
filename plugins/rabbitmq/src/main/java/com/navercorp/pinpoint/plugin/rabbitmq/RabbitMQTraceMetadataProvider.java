package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author Jinkai.Ma
 */
public class RabbitMQTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(RabbitMQConstants.RABBITMQ_SERVICE_TYPE);

        context.addAnnotationKey(RabbitMQConstants.RABBITMQ_EXCHANGE_ANNOTATION_KEY);
        context.addAnnotationKey(RabbitMQConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY);
        context.addAnnotationKey(RabbitMQConstants.RABBITMQ_PROPERTIES_ANNOTATION_KEY);
        context.addAnnotationKey(RabbitMQConstants.RABBITMQ_BODY_ANNOTATION_KEY);
    }
}
