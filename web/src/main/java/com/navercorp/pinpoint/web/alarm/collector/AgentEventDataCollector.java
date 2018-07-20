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

package com.navercorp.pinpoint.web.alarm.collector;

import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class AgentEventDataCollector extends DataCollector {

    private final Application application;

    private final ApplicationIndexDao applicationIndexDao;
    private final AgentEventDao agentEventDao;

    private final long timeSlotEndTime;
    private final long slotInterval;
    private final AtomicBoolean init = new AtomicBoolean(false); // need to consider a race condition when checkers start simultaneously.

    private final Map<String, Boolean> agentDeadlockEventDetected = new HashMap<>();

    public AgentEventDataCollector(DataCollectorFactory.DataCollectorCategory dataCollectorCategory, Application application, AgentEventDao agentEventDao, ApplicationIndexDao applicationIndexDao, long timeSlotEndTime, long slotInterval) {
        super(dataCollectorCategory);
        this.application = application;

        this.agentEventDao = agentEventDao;
        this.applicationIndexDao = applicationIndexDao;

        this.timeSlotEndTime = timeSlotEndTime;

        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        Range range = Range.createUncheckedRange(timeSlotEndTime - slotInterval, timeSlotEndTime);
        List<String> agentIds = applicationIndexDao.selectAgentIds(application.getName());

        for (String agentId : agentIds) {
            List<AgentEventBo> agentEventBoList = agentEventDao.getAgentEvents(agentId, range, Collections.emptySet());
            if (hasDeadlockEvent(agentEventBoList)) {
                agentDeadlockEventDetected.put(agentId, true);
            }
        }

        init.set(true);
    }

    private boolean hasDeadlockEvent(List<AgentEventBo> agentEventBoList) {
        for (AgentEventBo agentEvent : agentEventBoList) {
            AgentEventType eventType = agentEvent.getEventType();
            if (eventType == AgentEventType.AGENT_DEADLOCK_DETECTED) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Boolean> getAgentDeadlockEventDetected() {
        return agentDeadlockEventDetected;
    }

}
