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

package com.navercorp.pinpoint.plugin.rabbitmq.client;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author Jinkai.Ma
 */
public class RabbitMQClientTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT);
        context.addServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL);

        context.addAnnotationKey(RabbitMQClientConstants.RABBITMQ_EXCHANGE_ANNOTATION_KEY);
        context.addAnnotationKey(RabbitMQClientConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY);
        context.addAnnotationKey(RabbitMQClientConstants.RABBITMQ_PROPERTIES_ANNOTATION_KEY);
        context.addAnnotationKey(RabbitMQClientConstants.RABBITMQ_BODY_ANNOTATION_KEY);
    }
}
