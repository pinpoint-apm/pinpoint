/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.google.inject.Inject;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSpanFactory implements SpanFactory {

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final ServiceType applicationServiceType;

    @Inject
    public DefaultSpanFactory(@ApplicationName String applicationName, @AgentId String agentId, @AgentStartTime long agentStartTime,
                                   @ApplicationServerType ServiceType applicationServiceType) {

        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationServiceType == null) {
            throw new NullPointerException("applicationServiceType must not be null");
        }

        this.applicationName = applicationName;
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.applicationServiceType = applicationServiceType;
    }

    @Override
    public Span newSpan() {
        final Span span = new Span();
        span.setAgentId(agentId);
        span.setApplicationName(applicationName);
        span.setAgentStartTime(agentStartTime);
        span.setApplicationServiceType(applicationServiceType.getCode());
        span.markBeforeTime();
        return span;
    }

}
