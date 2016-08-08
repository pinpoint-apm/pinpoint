/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT, AnnotationKeyMatchers.exact(AnnotationKey.MESSAGE_QUEUE_URI));
        context.addServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL, AnnotationKeyMatchers.ARGS_MATCHER);

        context.addAnnotationKey(ActiveMQClientConstants.ACTIVEMQ_BROKER_URL);
        context.addAnnotationKey(ActiveMQClientConstants.ACTIVEMQ_MESSAGE);
    }

}
