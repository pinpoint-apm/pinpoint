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

    String META_DO_NOT_TRACE = "_RABBITMQ_DO_NOT_TRACE";
    String META_TRANSACTION_ID = "_RABBITMQ_TRASACTION_ID";
    String META_SPAN_ID = "_RABBITMQ_SPAN_ID";
    String META_PARENT_SPAN_ID = "_RABBITMQ_PARENT_SPAN_ID";
    String META_PARENT_APPLICATION_NAME = "_RABBITMQ_PARENT_APPLICATION_NAME";
    String META_PARENT_APPLICATION_TYPE = "_RABBITMQ_PARENT_APPLICATION_TYPE";
    String META_FLAGS = "_RABBITMQ_FLAGS";
}
