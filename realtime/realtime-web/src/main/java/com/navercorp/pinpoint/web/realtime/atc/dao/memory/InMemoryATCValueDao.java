/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.realtime.atc.dao.memory;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author youngjin.kim2
 */
public class InMemoryATCValueDao implements ATCValueDao {

    private final long supplyExpireInNanos;

    private final Map<ClusterKey, Record> recordMap = new ConcurrentHashMap<>();
    private final Map<String, List<ClusterKey>> recentActiveAgentMap = new ConcurrentHashMap<>();

    public InMemoryATCValueDao(long supplyExpireInNanos) {
        this.supplyExpireInNanos = supplyExpireInNanos;
    }

    @Override
    public List<Integer> query(ClusterKey clusterKey, long baseTime) {
        final Record record = recordMap.get(clusterKey);
        if (record == null) {
            return null;
        }

        final long age = baseTime - record.observedAt;
        if (age > this.supplyExpireInNanos) {
            return null;
        }

        return record.values;
    }

    @Override
    public void put(String applicationName, ATCSupply ATCSupply) {
        final ClusterKey clusterKey = new ClusterKey(
                applicationName,
                ATCSupply.getAgentId(),
                ATCSupply.getStartTimestamp()
        );

        final Record record = new Record(ATCSupply.getValues(), System.nanoTime());

        recordMap.put(clusterKey, record);
    }

    @Override
    public void saveActiveAgents(String applicationName, List<ClusterKey> agents) {
        recentActiveAgentMap.put(applicationName, agents);
    }

    @Override
    public List<ClusterKey> getActiveAgents(String applicationName) {
        final List<ClusterKey> agents = recentActiveAgentMap.get(applicationName);
        if (agents == null) {
            return Collections.emptyList();
        }
        return agents;
    }

    private static class Record {

        final List<Integer> values;
        final long observedAt;

        public Record(List<Integer> values, long observedAt) {
            this.values = values;
            this.observedAt = observedAt;
        }

    }

}
