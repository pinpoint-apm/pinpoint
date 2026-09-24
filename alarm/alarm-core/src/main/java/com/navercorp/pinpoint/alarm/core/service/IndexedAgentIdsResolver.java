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

import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.bo.AgentIdEntry;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.common.timeseries.time.Range;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Lists an application's agents from the application index.
 *
 * <p>Not from what an agent recently reported, which looks cheaper and reads as a liveness
 * check but assumes an unhealthy agent keeps reporting. A jvm deadlocked badly enough stops:
 * the thread collecting its stats can be one of the stuck ones. Scoping by reported stats
 * therefore hides exactly the agents a deadlock rule is looking for.
 *
 * <p>It does bound how much of the index's history is read, by the lifecycle state the
 * collector's keep-alive rewrites every five minutes. The lookback is a day -- far enough
 * above that interval that it cannot decide which agents are seen -- and rows carrying no
 * state at all are kept rather than guessed about, so the bound is partial by design.
 */
public class IndexedAgentIdsResolver implements AgentIdsResolver {

    /**
     * How far before the rule's own window an agent's lifecycle state may sit and still be
     * asked about. A day, so that the bound never decides which agents a rule sees -- only
     * how much of the index's history the scan carries.
     */
    static final Duration DEFAULT_LOOKBACK = Duration.ofDays(1);

    private final AgentIdDao agentIdDao;
    private final Duration lookback;

    public IndexedAgentIdsResolver(AgentIdDao agentIdDao) {
        this(agentIdDao, DEFAULT_LOOKBACK);
    }

    IndexedAgentIdsResolver(AgentIdDao agentIdDao, Duration lookback) {
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.lookback = Objects.requireNonNull(lookback, "lookback");
    }

    @Override
    public List<String> resolve(Application application, Range window) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntryByMinStateTimestamp(
                application.getService().getServiceUid(),
                application.getApplicationName(),
                application.getServiceType().getCode(),
                window.getFrom() - lookback.toMillis());
        // One row per start time, so a restarted agent arrives more than once.
        return entries.stream().map(AgentIdEntry::getAgentId).distinct().sorted().toList();
    }
}
