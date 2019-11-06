/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.rabbitmq.client;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.QUEUE;

/**
 * @author Jinkai.Ma
 * @author HyunGil Jeong
 */
public final class RabbitMQClientConstants {
    private RabbitMQClientConstants() {
    }

    public static final ServiceType RABBITMQ_CLIENT = ServiceTypeFactory.of(8300, "RABBITMQ_CLIENT", QUEUE, RECORD_STATISTICS);
    public static final ServiceType RABBITMQ_CLIENT_INTERNAL = ServiceTypeFactory.of(8301, "RABBITMQ_CLIENT_INTERNAL", "RABBITMQ_CLIENT");

    public static final String RABBITMQ_PRODUCER_SCOPE = "rabbitmqProducerScope";
    public static final String RABBITMQ_CONSUMER_SCOPE = "rabbitmqConsumerScope";
    public static final String RABBITMQ_FRAME_HANDLER_CREATION_SCOPE = "rabbitmqFrameHandlerCreationScope";
    public static final String RABBITMQ_TEMPLATE_API_SCOPE = "rabbitmqTemplateApiScope";

    public static final AnnotationKey RABBITMQ_EXCHANGE_ANNOTATION_KEY = AnnotationKeyFactory.of(130, "rabbitmq.exchange", VIEW_IN_RECORD_SET);
    public static final AnnotationKey RABBITMQ_ROUTINGKEY_ANNOTATION_KEY = AnnotationKeyFactory.of(131, "rabbitmq.routingkey", VIEW_IN_RECORD_SET);
    public static final AnnotationKey RABBITMQ_PROPERTIES_ANNOTATION_KEY = AnnotationKeyFactory.of(132, "rabbitmq.properties");
    public static final AnnotationKey RABBITMQ_BODY_ANNOTATION_KEY = AnnotationKeyFactory.of(133, "rabbitmq.body");

    public static final String UNKNOWN = "Unknown";

    public static final String META_TRACE_ID = "Pinpoint-TraceID";
    public static final String META_SPAN_ID = "Pinpoint-SpanID";
    public static final String META_PARENT_SPAN_ID = "Pinpoint-pSpanID";
    public static final String META_SAMPLED = "Pinpoint-Sampled";
    public static final String META_FLAGS = "Pinpoint-Flags";
    public static final String META_PARENT_APPLICATION_NAME = "Pinpoint-pAppName";
    public static final String META_PARENT_APPLICATION_TYPE = "Pinpoint-pAppType";
}
