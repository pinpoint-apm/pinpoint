package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 */
public class ConsumerHandleDeliveryInterceptor extends RabbitMQConsumeInterceptor {

    public ConsumerHandleDeliveryInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ConsumerHandleDeliveryInterceptor.class);
    }

}
