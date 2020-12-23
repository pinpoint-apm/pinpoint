/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rocketmq;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.QUEUE;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import java.nio.charset.Charset;

import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

public class RocketMQConstants {

    public static final String SCOPE = "ROCKETMQ_SCOPE";
    public static final String ENDPOINT = "ROCKETMQ_ENDPOINT";

    public static final ServiceType ROCKETMQ_CLIENT = ServiceTypeFactory.of(8400, "ROCKETMQ_CLIENT", "ROCKETMQ_CLIENT", QUEUE, RECORD_STATISTICS);
    public static final ServiceType ROCKETMQ_CLIENT_INTERNAL = ServiceTypeFactory.of(8401, "ROCKETMQ_CLIENT_INTERNAL", "ROCKETMQ_CLIENT");

    public static final AnnotationKey ROCKETMQ_TOPIC_ANNOTATION_KEY = AnnotationKeyFactory.of(800, "rocketmq.topic", VIEW_IN_RECORD_SET);
    public static final AnnotationKey ROCKETMQ_PARTITION_ANNOTATION_KEY = AnnotationKeyFactory.of(801, "rocketmq.message.queue", VIEW_IN_RECORD_SET);
    public static final AnnotationKey ROCKETMQ_OFFSET_ANNOTATION_KEY = AnnotationKeyFactory.of(802, "rocketmq.offset", VIEW_IN_RECORD_SET);
    public static final AnnotationKey ROCKETMQ_BATCH_ANNOTATION_KEY = AnnotationKeyFactory.of(803, "rocketmq.batch", VIEW_IN_RECORD_SET);

    public static final String UNKNOWN = "Unknown";


}
