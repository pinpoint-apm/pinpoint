/*
 * Copyright 2021 NAVER Corp.
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

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author messi-gao
 */
public class RocketMQMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(RocketMQConstants.ROCKETMQ_CLIENT);
        context.addServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);

        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_TOPIC_ANNOTATION_KEY);
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_PARTITION_ANNOTATION_KEY);
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_OFFSET_ANNOTATION_KEY);
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_BATCH_ANNOTATION_KEY);
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_SEND_STATUS_ANNOTATION_KEY);
        context.addAnnotationKey(RocketMQConstants.ROCKETMQ_BROKER_SERVER_STATUS_ANNOTATION_KEY);
    }

}
