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
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

/**
 * @author HyunGil Jeong
 */
public final class ActiveMQClientConstants {
    private ActiveMQClientConstants() {
    }

    public static final ServiceType ACTIVEMQ_CLIENT = ServiceTypeProvider.getByCode(8310);
    public static final ServiceType ACTIVEMQ_CLIENT_INTERNAL = ServiceTypeProvider.getByName("ACTIVEMQ_CLIENT_INTERNAL");

    public static final AnnotationKey ACTIVEMQ_BROKER_URL = AnnotationKeyProvider.getByCode(101);
    public static final AnnotationKey ACTIVEMQ_MESSAGE = AnnotationKeyProvider.getByCode(102);

    public static final String UNKNOWN_ADDRESS = "Unknown";

    public static final String ACTIVEMQ_CLIENT_SCOPE = "ActiveMQClientScope";
}
