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

package com.navercorp.pinpoint.plugin.loggingevent;

import com.navercorp.pinpoint.common.trace.*;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;


public class LoggingEventTraceMetadataProvider implements TraceMetadataProvider {

    public static final ServiceType LOGGING_EVENT = ServiceTypeFactory.of(1910, "LOGGING_EVENT", RECORD_STATISTICS);
    public static final ServiceType LOGGING_EVENT_METHOD = ServiceTypeFactory.of(1911, "LOGGING_METHOD");

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(LOGGING_EVENT);
        context.addServiceType(LOGGING_EVENT_METHOD);
    }

}
