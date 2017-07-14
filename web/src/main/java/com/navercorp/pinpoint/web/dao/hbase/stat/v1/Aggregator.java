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

package com.navercorp.pinpoint.web.dao.hbase.stat.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import org.springframework.stereotype.Component;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 */
@Deprecated
public interface Aggregator<T extends AgentStatDataPoint> {

    long AGGR_SAMPLE_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    long RAW_SAMPLE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    Comparator<AgentStatDataPoint> TIMESTAMP_COMPARATOR = new Comparator<AgentStatDataPoint>() {

        @Override
        public int compare(AgentStatDataPoint o1, AgentStatDataPoint o2) {
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
    };

    List<T> aggregate(List<T> statsToAggregate, long interval);

    abstract class AbstractAggregator<T extends AgentStatDataPoint> implements Aggregator<T> {

        private AbstractAggregator() {
        }

        public List<T> aggregate(List<T> statsToAggregate, long interval) {
            if (statsToAggregate.isEmpty()) {
                return statsToAggregate;
            }
            List<T> stats = new ArrayList<>(statsToAggregate);
            Collections.sort(stats, TIMESTAMP_COMPARATOR);

            List<T> result = new ArrayList<>();
            T current = null;
            for (T stat : stats) {
                long normalizedTimestamp = normalizeTimestamp(interval, stat.getTimestamp());
                if (current == null) {
                    current = normalizeAgentStat(stat, interval, normalizedTimestamp);
                } else {
                    if (current.getTimestamp() == normalizedTimestamp) {
                        current = merge(current, stat, interval);
                    } else {
                        result.add(current);
                        current = normalizeAgentStat(stat, interval, normalizedTimestamp);
                    }
                }
            }
            result.add(current);
            return result;
        }

        private long normalizeTimestamp(long interval, long timestamp) {
            long normalizedTimestamp = (timestamp / interval) * interval;
            return normalizedTimestamp;
        }

        private T normalizeAgentStat(T stat, long interval, long normalizedTimestamp) {
            T result = createNormalizedAgentStat(stat, normalizedTimestamp, interval);
            return result;
        }

        protected final long addUnsignedLongs(long v1, long v2) {
            if (v1 < 0) {
                if (v2 < 0) {
                    return -1;
                } else {
                    return v2;
                }
            } else {
                if (v2 < 0) {
                    return v1;
                } else {
                    return v1 + v2;
                }
            }
        }

        protected final long maxUnsignedLongs(long v1, long v2) {
            if (v1 < 0) {
                return v2;
            } else if (v2 < 0) {
                return v1;
            }

            return v1 < v2 ? v2 : v1;
        }

        protected final T getLatest(T s1, T s2) {
            return s1.getTimestamp() > s2.getTimestamp() ? s1 : s2;
        }

        protected abstract T createNormalizedAgentStat(T src, long normalizedTimestamp, long interval);

        protected abstract T merge(T s1, T s2, long interval);
    }

    @Deprecated
    @Component
    class JvmGcAggregator extends AbstractAggregator<JvmGcBo> {

        @Override
        protected JvmGcBo createNormalizedAgentStat(JvmGcBo src, long normalizedTimestamp, long interval) {
            JvmGcBo normalized = new JvmGcBo();
            normalized.setAgentId(src.getAgentId());
            normalized.setTimestamp(normalizedTimestamp);
            normalized.setGcType(src.getGcType());
            normalized.setHeapUsed(src.getHeapUsed());
            normalized.setHeapMax(src.getHeapMax());
            normalized.setNonHeapUsed(src.getNonHeapUsed());
            normalized.setNonHeapMax(src.getNonHeapMax());
            normalized.setGcOldCount(src.getGcOldCount());
            normalized.setGcOldTime(src.getGcOldTime());
            return normalized;
        }

        @Override
        protected JvmGcBo merge(JvmGcBo s1, JvmGcBo s2, long interval) {
            JvmGcBo latest = getLatest(s1, s2);
            JvmGcBo merged = new JvmGcBo();
            merged.setAgentId(latest.getAgentId());
            merged.setTimestamp(latest.getTimestamp());
            merged.setGcType(latest.getGcType());
            merged.setHeapUsed(latest.getHeapUsed());
            merged.setHeapMax(maxUnsignedLongs(s1.getHeapMax(), s2.getHeapMax()));
            merged.setNonHeapUsed(latest.getNonHeapUsed());
            merged.setNonHeapMax(maxUnsignedLongs(s1.getNonHeapMax(), s2.getNonHeapMax()));
            merged.setGcOldCount(latest.getGcOldCount());
            merged.setGcOldTime(latest.getGcOldTime());
            return merged;
        }
    }

    @Deprecated
    @Component
    class JvmGcDetailedAggregator extends AbstractAggregator<JvmGcDetailedBo> {

        @Override
        protected JvmGcDetailedBo createNormalizedAgentStat(JvmGcDetailedBo src, long normalizedTimestamp, long interval) {
            JvmGcDetailedBo normalized = new JvmGcDetailedBo();
            normalized.setAgentId(src.getAgentId());
            normalized.setTimestamp(normalizedTimestamp);
            normalized.setGcNewCount(src.getGcNewCount());
            normalized.setGcNewTime(src.getGcNewTime());
            normalized.setCodeCacheUsed(src.getCodeCacheUsed());
            normalized.setNewGenUsed(src.getNewGenUsed());
            normalized.setOldGenUsed(src.getOldGenUsed());
            normalized.setSurvivorSpaceUsed(src.getSurvivorSpaceUsed());
            normalized.setPermGenUsed(src.getPermGenUsed());
            normalized.setMetaspaceUsed(src.getMetaspaceUsed());
            return normalized;
        }

        @Override
        protected JvmGcDetailedBo merge(JvmGcDetailedBo s1, JvmGcDetailedBo s2, long interval) {
            JvmGcDetailedBo latest = getLatest(s1, s2);
            JvmGcDetailedBo merged = new JvmGcDetailedBo();
            merged.setAgentId(latest.getAgentId());
            merged.setTimestamp(latest.getTimestamp());
            merged.setGcNewCount(latest.getGcNewCount());
            merged.setGcNewTime(latest.getGcNewTime());
            merged.setCodeCacheUsed(latest.getCodeCacheUsed());
            merged.setNewGenUsed(latest.getNewGenUsed());
            merged.setOldGenUsed(latest.getOldGenUsed());
            merged.setSurvivorSpaceUsed(latest.getSurvivorSpaceUsed());
            merged.setPermGenUsed(latest.getPermGenUsed());
            merged.setMetaspaceUsed(latest.getMetaspaceUsed());
            return merged;
        }
    }

    @Deprecated
    @Component
    class CpuLoadAggregator extends AbstractAggregator<CpuLoadBo> {

        @Override
        protected CpuLoadBo createNormalizedAgentStat(CpuLoadBo src, long normalizedTimestamp, long interval) {
            CpuLoadBo normalized = new CpuLoadBo();
            normalized.setAgentId(src.getAgentId());
            normalized.setTimestamp(normalizedTimestamp);
            normalized.setJvmCpuLoad(src.getJvmCpuLoad());
            normalized.setSystemCpuLoad(src.getSystemCpuLoad());
            return normalized;
        }

        @Override
        protected CpuLoadBo merge(CpuLoadBo s1, CpuLoadBo s2, long interval) {
            CpuLoadBo latest = getLatest(s1, s2);
            CpuLoadBo merged = new CpuLoadBo();
            merged.setAgentId(latest.getAgentId());
            merged.setTimestamp(latest.getTimestamp());
            merged.setJvmCpuLoad(latest.getJvmCpuLoad());
            merged.setSystemCpuLoad(latest.getSystemCpuLoad());
            return merged;
        }
    }

    @Deprecated
    @Component
    class TransactionAggregator extends AbstractAggregator<TransactionBo> {

        @Override
        protected TransactionBo createNormalizedAgentStat(TransactionBo src, long normalizedTimestamp, long interval) {
            TransactionBo normalized = new TransactionBo();
            normalized.setAgentId(src.getAgentId());
            normalized.setTimestamp(normalizedTimestamp);
            normalized.setCollectInterval(interval);
            normalized.setSampledNewCount(src.getSampledNewCount());
            normalized.setSampledContinuationCount(src.getSampledContinuationCount());
            normalized.setUnsampledNewCount(src.getUnsampledNewCount());
            normalized.setUnsampledContinuationCount(src.getUnsampledContinuationCount());
            return normalized;
        }

        @Override
        protected TransactionBo merge(TransactionBo s1, TransactionBo s2, long interval) {
            TransactionBo latest = getLatest(s1, s2);
            TransactionBo merged = new TransactionBo();
            merged.setAgentId(latest.getAgentId());
            merged.setTimestamp(latest.getTimestamp());
            merged.setSampledNewCount(addUnsignedLongs(s1.getSampledNewCount(), s2.getSampledNewCount()));
            merged.setSampledContinuationCount(addUnsignedLongs(s1.getSampledContinuationCount(), s2.getSampledContinuationCount()));
            merged.setUnsampledNewCount(addUnsignedLongs(s1.getUnsampledNewCount(), s2.getUnsampledNewCount()));
            merged.setUnsampledContinuationCount(addUnsignedLongs(s1.getUnsampledContinuationCount(), s2.getUnsampledContinuationCount()));
            return merged;
        }
    }

    @Deprecated
    @Component
    class ActiveTraceAggregator extends AbstractAggregator<ActiveTraceBo> {

        @Override
        protected ActiveTraceBo createNormalizedAgentStat(ActiveTraceBo src, long normalizedTimestamp, long interval) {
            ActiveTraceBo normalized = new ActiveTraceBo();
            normalized.setAgentId(src.getAgentId());
            normalized.setTimestamp(normalizedTimestamp);
            normalized.setVersion(src.getVersion());
            normalized.setHistogramSchemaType(src.getHistogramSchemaType());
            normalized.setActiveTraceCounts(src.getActiveTraceCounts());
            return normalized;
        }

        @Override
        protected ActiveTraceBo merge(ActiveTraceBo s1, ActiveTraceBo s2, long interval) {
            ActiveTraceBo latest = getLatest(s1, s2);
            ActiveTraceBo merged = new ActiveTraceBo();
            merged.setAgentId(latest.getAgentId());
            merged.setTimestamp(latest.getTimestamp());
            merged.setVersion(latest.getVersion());
            merged.setHistogramSchemaType(latest.getHistogramSchemaType());
            merged.setActiveTraceCounts(latest.getActiveTraceCounts());
            return merged;
        }
    }
}