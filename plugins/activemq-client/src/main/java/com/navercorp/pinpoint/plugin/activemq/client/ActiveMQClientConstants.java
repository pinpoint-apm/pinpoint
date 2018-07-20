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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProperty;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.QUEUE;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author HyunGil Jeong
 */
public final class ActiveMQClientConstants {
    private ActiveMQClientConstants() {
    }

    public static final ServiceType ACTIVEMQ_CLIENT = ServiceTypeFactory.of(8310, "ACTIVEMQ_CLIENT", QUEUE, RECORD_STATISTICS);
    public static final ServiceType ACTIVEMQ_CLIENT_INTERNAL = ServiceTypeFactory.of(8311, "ACTIVEMQ_CLIENT_INTERNAL", "ACTIVEMQ_CLIENT");

    public static final AnnotationKey ACTIVEMQ_BROKER_URL = AnnotationKeyFactory.of(101, "activemq.broker.address", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey ACTIVEMQ_MESSAGE = AnnotationKeyFactory.of(102, "activemq.message", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final String UNKNOWN_ADDRESS = "Unknown";

    public static final String ACTIVEMQ_CLIENT_SCOPE = "ActiveMQClientScope";
}
