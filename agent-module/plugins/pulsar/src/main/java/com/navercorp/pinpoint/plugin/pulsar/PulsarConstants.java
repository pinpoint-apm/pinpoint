/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.pulsar;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.QUEUE;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author zhouzixin@apache.org
 */
public class PulsarConstants {

    public static final String ACCEPTOR_HOST = "PULSAR_ACCEPTOR_HOST";
    public static final String IS_ASYNC_SEND = "IS_ASYNC_SEND";
    public static final String SCOPE = "PULSAR_SEND_COMPLETED";

    public static final ServiceType PULSAR_CLIENT = ServiceTypeFactory.of(
            8663, "PULSAR_CLIENT", "PULSAR_CLIENT", QUEUE, RECORD_STATISTICS);
    public static final ServiceType PULSAR_CLIENT_INTERNAL = ServiceTypeFactory.of(
            8664, "PULSAR_CLIENT_INTERNAL", "PULSAR_CLIENT_INTERNAL", QUEUE, RECORD_STATISTICS);

    public static final AnnotationKey PULSAR_RETRY_COUNT_ANNOTATION_KEY =
            AnnotationKeyFactory.of(893, "pulsar.retry.count", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PULSAR_SEQUENCE_ID_ANNOTATION_KEY =
            AnnotationKeyFactory.of(894, "pulsar.sequence.id", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PULSAR_MESSAGE_SIZE_ANNOTATION_KEY =
            AnnotationKeyFactory.of(895, "pulsar.message.size", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PULSAR_PARTITION_ANNOTATION_KEY =
            AnnotationKeyFactory.of(896, "pulsar.partition.index", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PULSAR_MESSAGE_ID_ANNOTATION_KEY =
            AnnotationKeyFactory.of(897, "pulsar.message.id", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PULSAR_TOPIC_ANNOTATION_KEY =
            AnnotationKeyFactory.of(898, "pulsar.topic", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PULSAR_BROKER_URL_ANNOTATION_KEY =
            AnnotationKeyFactory.of(899, "pulsar.broker.url", VIEW_IN_RECORD_SET);
}
