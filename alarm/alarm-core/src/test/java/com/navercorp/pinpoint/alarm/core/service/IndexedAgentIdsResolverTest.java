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
import com.navercorp.pinpoint.common.server.bo.AgentStatus;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * That the index is asked about the application the rule actually names.
 *
 * <p>All three parts of the row key prefix come off the {@link Application}. Getting the
 * service uid wrong reads a different service's slice of the index and answers with nothing,
 * which the deadlock metric cannot tell apart from an application that is fine -- the same
 * class of silent miss this resolver exists to remove.
 */
class IndexedAgentIdsResolverTest {

    private static final Service TEST_SERVICE = new Service("svc", 42);

    @Test
    void theRuleApplicationDecidesWhichSliceOfTheIndexIsRead() {
        RecordingDao dao = new RecordingDao(List.of("agent-a", "agent-b"));
        Application application =
                new Application(TEST_SERVICE, "app", ServiceType.TEST_STAND_ALONE);

        List<String> agentIds = new IndexedAgentIdsResolver(dao)
                .resolve(application, Range.between(0L, 1000L));

        assertEquals(List.of("agent-a", "agent-b"), agentIds);
        assertEquals(42, dao.serviceUid);
        assertEquals("app", dao.applicationName);
        assertEquals(ServiceType.TEST_STAND_ALONE.getCode(), dao.serviceTypeCode);
    }

    // The bound exists to keep the scan off history the cleanup job has not reached, not to
    // decide which agents a rule sees -- an agent quiet since before the window still counts,
    // because going quiet is what a deadlock looks like from the outside.
    @Test
    void theStateBoundSitsAWholeLookbackBeforeTheWindow() {
        RecordingDao dao = new RecordingDao(List.of("agent-a"));
        Application application =
                new Application(TEST_SERVICE, "app", ServiceType.TEST_STAND_ALONE);
        long from = 1_000_000L;

        new IndexedAgentIdsResolver(dao, Duration.ofDays(1))
                .resolve(application, Range.between(from, from + 60_000L));

        assertEquals(from - Duration.ofDays(1).toMillis(), dao.minStateTimestamp);
    }

    private static class RecordingDao implements AgentIdDao {
        private final List<String> agentIds;
        private int serviceUid;
        private String applicationName;
        private int serviceTypeCode;
        private long minStateTimestamp;

        RecordingDao(List<String> agentIds) {
            this.agentIds = agentIds;
        }

        @Override
        public List<AgentIdEntry> getAgentIdEntryByMinStateTimestamp(
                int serviceUid, String applicationName, int serviceTypeCode, long minStateTimestamp) {
            this.serviceUid = serviceUid;
            this.applicationName = applicationName;
            this.serviceTypeCode = serviceTypeCode;
            this.minStateTimestamp = minStateTimestamp;
            return agentIds.stream().map(RecordingDao::entry).toList();
        }

        private static AgentIdEntry entry(String agentId) {
            return new AgentIdEntry(
                    new Application(TEST_SERVICE, "app", ServiceType.TEST_STAND_ALONE),
                    agentId, 0L, null, AgentLifeCycleState.RUNNING, 0L);
        }

        @Override
        public List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName,
                                                  int serviceTypeCode, String agentId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(int serviceUid, String applicationName, int serviceTypeCode,
                           String agentId, long agentStartTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(List<AgentIdEntry> agentIdEntryList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(int serviceUid, String applicationName, int serviceTypeCode, String agentId,
                           long agentStartTime, String agentName, AgentStatus agentStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int countAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AgentIdEntry> getInactiveAgentIdEntry(long maxStatusTimestamp, int limit,
                                                          AgentIdEntry lastAgentIdEntry) {
            throw new UnsupportedOperationException();
        }
    }
}
