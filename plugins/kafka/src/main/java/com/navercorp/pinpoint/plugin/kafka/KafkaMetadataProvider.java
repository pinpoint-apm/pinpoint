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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class KafkaMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(KafkaConstants.KAFKA_CLIENT);
        context.addServiceType(KafkaConstants.KAFKA_CLIENT_INTERNAL);
        context.addServiceType(KafkaConstants.KAFKA_STREAMS);

        context.addAnnotationKey(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY);
        context.addAnnotationKey(KafkaConstants.KAFKA_PARTITION_ANNOTATION_KEY);
        context.addAnnotationKey(KafkaConstants.KAFKA_OFFSET_ANNOTATION_KEY);
        context.addAnnotationKey(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY);
        context.addAnnotationKey(KafkaConstants.KAFKA_HEADER_ANNOTATION_KEY);
    }

}
