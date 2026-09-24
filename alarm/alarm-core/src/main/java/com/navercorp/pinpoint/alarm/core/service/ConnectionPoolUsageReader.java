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
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentFieldUsage;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Reads how full each connection pool of each agent is.
 *
 * <p>Two levels rather than one: an agent has as many pools as it has data sources, each
 * identified by the url it connects to, so a value here belongs to a pool and not to the
 * agent that holds it. The keys it returns name both, which is what lets a notification say
 * which pool of which agent filled up.
 *
 * <p>The reads are issued per pool and collected afterwards, because an application with
 * twenty agents and three pools each would otherwise wait for sixty round trips in sequence.
 */
class ConnectionPoolUsageReader {

    private static final String METRIC_DATA_SOURCE = "dataSource";
    private static final String FIELD_ACTIVE_CONNECTION = "activeConnectionSize";
    private static final String FIELD_MAX_CONNECTION = "maxConnectionSize";
    private static final String TAG_JDBC_URL = "jdbcUrl";
    private static final String TAG_DATABASE_NAME = "databaseName";

    private final AgentStatAlarmDao agentStatAlarmDao;

    ConnectionPoolUsageReader(AgentStatAlarmDao agentStatAlarmDao) {
        this.agentStatAlarmDao = Objects.requireNonNull(agentStatAlarmDao, "agentStatAlarmDao");
    }

    /** @return usage percentage keyed by "agentId pool", empty when nothing reported */
    Map<String, Double> read(String applicationName, List<String> agentIds, Range window) {
        try {
            Map<String, Double> byPool = new LinkedHashMap<>();
            for (PoolRead read : issue(applicationName, agentIds, window)) {
                Usage usage = usageOf(read.future().get());
                if (usage.maxConnection() <= 0) {
                    // A pool that reported no ceiling has no rate to speak of. Treating the
                    // ceiling as zero would divide by it; reporting zero would call a full
                    // pool empty.
                    continue;
                }
                byPool.put(read.label(), (usage.activeConnection() * 100.0) / usage.maxConnection());
            }
            return byPool;
        } catch (ExecutionException e) {
            // Raised rather than answered with an empty map: a store that could not be read
            // is not a pool that is empty, and an absent value reads as a condition not met,
            // which would take a firing rule back to normal for the length of the outage.
            throw new IllegalStateException(
                    "Failed to read connection pool usage: application=" + applicationName,
                    Objects.requireNonNullElse(e.getCause(), e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted reading connection pool usage: application=" + applicationName, e);
        }
    }

    private List<PoolRead> issue(String applicationName, List<String> agentIds, Range window)
            throws ExecutionException, InterruptedException {
        List<PoolRead> reads = new ArrayList<>();
        for (Map.Entry<String, List<TagInformation>> entry
                : poolsByAgent(applicationName, agentIds, window).entrySet()) {
            for (TagInformation pool : entry.getValue()) {
                CompletableFuture<List<AgentFieldUsage>> future =
                        agentStatAlarmDao.selectAvgGroupByField(
                                applicationName, entry.getKey(), METRIC_DATA_SOURCE,
                                List.of(FIELD_ACTIVE_CONNECTION, FIELD_MAX_CONNECTION),
                                pool.tags(), window);
                reads.add(new PoolRead(future, label(entry.getKey(), pool)));
            }
        }
        return reads;
    }

    /**
     * Which pools each agent has. Discovered from the urls it reported connecting to, since
     * an agent's pools are not declared anywhere the alarm can read.
     */
    private Map<String, List<TagInformation>> poolsByAgent(String applicationName,
                                                           List<String> agentIds, Range window)
            throws ExecutionException, InterruptedException {
        List<TagRead> tagReads = new ArrayList<>();
        for (String agentId : agentIds) {
            tagReads.add(new TagRead(agentStatAlarmDao.selectTagInfo(
                    applicationName, agentId, METRIC_DATA_SOURCE, FIELD_ACTIVE_CONNECTION,
                    window), agentId));
        }

        List<PoolTagRead> poolReads = new ArrayList<>();
        for (TagRead tagRead : tagReads) {
            for (Tag tag : tagRead.future().get()) {
                if (!TAG_JDBC_URL.equals(tag.getName())) {
                    continue;
                }
                poolReads.add(new PoolTagRead(agentStatAlarmDao.getTagInfoContainedSpecificTag(
                        applicationName, tagRead.agentId(), METRIC_DATA_SOURCE,
                        FIELD_ACTIVE_CONNECTION, List.of(tag), window), tagRead.agentId()));
            }
        }

        Map<String, List<TagInformation>> byAgent = new LinkedHashMap<>();
        for (PoolTagRead poolRead : poolReads) {
            byAgent.computeIfAbsent(poolRead.agentId(), id -> new ArrayList<>())
                    .addAll(poolRead.future().get());
        }
        return byAgent;
    }

    private Usage usageOf(List<AgentFieldUsage> fields) {
        double active = 0;
        double max = 0;
        for (AgentFieldUsage field : fields) {
            if (FIELD_ACTIVE_CONNECTION.equals(field.getFieldName())) {
                active = field.getValue() == null ? 0 : field.getValue();
            } else if (FIELD_MAX_CONNECTION.equals(field.getFieldName())) {
                max = field.getValue() == null ? 0 : field.getValue();
            }
        }
        return new Usage(active, max);
    }

    /** Names the database rather than the url, which carries credentials in some drivers. */
    private String label(String agentId, TagInformation pool) {
        for (Tag tag : pool.tags()) {
            if (TAG_DATABASE_NAME.equals(tag.getName())) {
                return agentId + " " + tag.getValue();
            }
        }
        return agentId;
    }

    private record PoolRead(CompletableFuture<List<AgentFieldUsage>> future, String label) {
    }

    private record TagRead(CompletableFuture<List<Tag>> future, String agentId) {
    }

    private record PoolTagRead(CompletableFuture<List<TagInformation>> future, String agentId) {
    }

    private record Usage(double activeConnection, double maxConnection) {
    }
}
