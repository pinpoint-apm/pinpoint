/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter.agent;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public interface AgentEventFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean accept(AgentEvent agentEvent);

    class ExcludeFilter implements AgentEventFilter {

        private final Set<AgentEventType> excludedEventTypes;

        public ExcludeFilter(AgentEventType... excludeEventTypes) {
            if (ArrayUtils.isEmpty(excludeEventTypes)) {
                this.excludedEventTypes = Collections.emptySet();
            } else {
                this.excludedEventTypes = new HashSet<>(Arrays.asList(excludeEventTypes));
            }
        }

        public ExcludeFilter(int... excludeEventTypeCodes) {
            if (ArrayUtils.isEmpty(excludeEventTypeCodes)) {
                excludedEventTypes = Collections.emptySet();
                return;
            }
            excludedEventTypes = new HashSet<>();
            for (int excludeEventTypeCode : excludeEventTypeCodes) {
                AgentEventType excludedEventType = AgentEventType.getTypeByCode(excludeEventTypeCode);
                if (excludedEventType != null) {
                    excludedEventTypes.add(excludedEventType);
                }
            }
        }

        @Override
        public boolean accept(AgentEvent agentEvent) {
            AgentEventType agentEventType = AgentEventType.getTypeByCode(agentEvent.getEventTypeCode());
            if (excludedEventTypes.contains(agentEventType)) {
                return REJECT;
            }
            return ACCEPT;
        }
    }
}
