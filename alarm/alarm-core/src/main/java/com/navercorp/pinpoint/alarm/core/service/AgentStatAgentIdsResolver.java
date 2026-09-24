/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.core.service;

import com.navercorp.pinpoint.alarm.core.agentstat.dao.AgentStatAlarmDao;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.timeseries.time.Range;

import java.util.List;
import java.util.Objects;

/**
 * Lists an application's agents from the stats they reported.
 *
 * <p>The right list for a usage metric: the value is an average or a rate over what the
 * agent reported, so one that reported nothing has nothing to compare. The wrong list for
 * anything asking whether an agent is in trouble rather than how loaded it is -- see
 * {@link IndexedAgentIdsResolver}.
 *
 * <p>It asks after the garbage collection metric because every agent reports it, which makes
 * it the cheapest way to ask the stat table which agents are alive -- the alternative is
 * scanning the table for every metric an agent might have sent.
 */
public class AgentStatAgentIdsResolver implements AgentIdsResolver {

    private static final String METRIC_REPORTED_BY_EVERY_AGENT = "jvmGc";

    private final AgentStatAlarmDao agentStatAlarmDao;

    public AgentStatAgentIdsResolver(AgentStatAlarmDao agentStatAlarmDao) {
        this.agentStatAlarmDao = Objects.requireNonNull(agentStatAlarmDao, "agentStatAlarmDao");
    }

    @Override
    public List<String> resolve(Application application, Range window) {
        return agentStatAlarmDao.selectAgentIds(
                application.getApplicationName(), METRIC_REPORTED_BY_EVERY_AGENT, window);
    }
}
