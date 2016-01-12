package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author Jinkai.Ma
 */
public interface RabbitMQConstants {
    ServiceType RABBITMQ_SERVICE_TYPE = ServiceTypeFactory.of(8300, "RABBITMQ", TERMINAL, RECORD_STATISTICS);
    String RABBITMQ_SCOPE = "rabbitmqScope";

    AnnotationKey RABBITMQ_EXCHANGE_ANNOTATION_KEY = AnnotationKeyFactory.of(100, "rabbitmq.exchange", VIEW_IN_RECORD_SET);
    AnnotationKey RABBITMQ_ROUTINGKEY_ANNOTATION_KEY = AnnotationKeyFactory.of(101, "rabbitmq.routingkey", VIEW_IN_RECORD_SET);
    AnnotationKey RABBITMQ_PROPERTIES_ANNOTATION_KEY = AnnotationKeyFactory.of(102, "rabbitmq.properties");
    AnnotationKey RABBITMQ_BODY_ANNOTATION_KEY = AnnotationKeyFactory.of(103, "rabbitmq.body");
}
