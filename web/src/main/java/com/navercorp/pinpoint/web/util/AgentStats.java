/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.navercorp.pinpoint.web.vo.AgentStat;

/**
 * @author Jongho Moon
 *
 */
public class AgentStats {
    public static final Comparator<AgentStat> TIMESTAMP_COMPARATOR = new Comparator<AgentStat>() {
        
        @Override
        public int compare(AgentStat o1, AgentStat o2) {
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
    };
    
    public static List<AgentStat> aggregate(List<AgentStat> stats, long newInterval) {
        return new Aggregator(stats, newInterval).aggregate();
    }
    
    
    private static class Aggregator {
        private final List<AgentStat> stats;
        private final long interval;
        
        public Aggregator(List<AgentStat> stats, long interval) {
            this.interval = interval;
            this.stats = new ArrayList<>(stats);
            Collections.sort(this.stats, TIMESTAMP_COMPARATOR);
        }

        public List<AgentStat> aggregate() {
            if (stats.isEmpty()) {
                return stats;
            }
            
            List<AgentStat> result = new ArrayList<>();
            AgentStat current = toAggregatedAgentStat(stats.get(0));
            
            for (AgentStat stat : stats.subList(1, stats.size())) {
                long timestamp = toAggregatedTimestamp(stat, interval);
                
                if (current.getTimestamp() == timestamp) {
                    current = merge(current, stat);
                } else {
                    result.add(current);
                    current = toAggregatedAgentStat(stat);
                }
            }
            
            result.add(current);
            
            return result;
        }

        private AgentStat toAggregatedAgentStat(AgentStat stat) {
            long timestamp = toAggregatedTimestamp(stat, interval);
            AgentStat result = new AgentStat(stat.getAgentId(), timestamp);
            
            result.setCollectInterval(interval);
            
            result.setGcType(stat.getGcType());
            result.setGcOldCount(stat.getGcOldCount());
            result.setGcOldTime(stat.getGcOldTime());
            
            result.setHeapUsed(stat.getHeapUsed());
            result.setHeapMax(stat.getHeapMax());
            
            result.setNonHeapUsed(stat.getNonHeapUsed());
            result.setNonHeapMax(stat.getNonHeapMax());
            
            result.setJvmCpuUsage(stat.getJvmCpuUsage());
            result.setSystemCpuUsage(stat.getSystemCpuUsage());
            
            result.setSampledNewCount(stat.getSampledNewCount());
            result.setSampledContinuationCount(stat.getSampledContinuationCount());
            result.setUnsampledNewCount(stat.getUnsampledNewCount());
            result.setUnsampledContinuationCount(stat.getUnsampledContinuationCount());
            
            result.setHistogramSchema(stat.getHistogramSchema());
            result.setActiveTraceCounts(stat.getActiveTraceCounts());

            return result;
        }
        
    }

    public static long toAggregatedTimestamp(AgentStat stat, long interval) {
        long timestamp = (stat.getTimestamp() / interval) * interval;
        
        if (stat.getTimestamp() != timestamp) {
            timestamp += interval;
        }
        
        return timestamp;
    }

    public static AgentStat merge(AgentStat s1, AgentStat s2) {
        AgentStat latest = s1.getTimestamp() > s2.getTimestamp() ? s1 : s2;

        AgentStat stat = new AgentStat(s1.getAgentId(), s1.getTimestamp());
        
        stat.setGcType(latest.getGcType());
        stat.setGcOldCount(latest.getGcOldCount());
        stat.setGcOldTime(latest.getGcOldTime());
        
        stat.setHeapUsed(latest.getHeapUsed());
        stat.setHeapMax(maxValue(s1.getHeapMax(), s2.getHeapMax()));
        
        stat.setNonHeapUsed(latest.getNonHeapUsed());
        stat.setNonHeapMax(maxValue(s1.getNonHeapMax(), s2.getNonHeapMax()));
        
        stat.setJvmCpuUsage(latest.getJvmCpuUsage());
        stat.setSystemCpuUsage(latest.getSystemCpuUsage());
        
        stat.setSampledNewCount(addValue(s1.getSampledNewCount(), s2.getSampledNewCount()));
        stat.setSampledContinuationCount(addValue(s1.getSampledContinuationCount(), s2.getSampledContinuationCount()));
        stat.setUnsampledNewCount(addValue(s1.getUnsampledNewCount(), s2.getUnsampledNewCount()));
        stat.setUnsampledContinuationCount(addValue(s1.getUnsampledContinuationCount(), s2.getUnsampledContinuationCount()));
        
        stat.setHistogramSchema(latest.getHistogramSchema());
        stat.setActiveTraceCounts(latest.getActiveTraceCounts());
        
        return stat;
    }
    
    private static long addValue(long v1, long v2) {
        if (v1 == AgentStat.NOT_COLLECTED) {
            if (v2 == AgentStat.NOT_COLLECTED) {
                return AgentStat.NOT_COLLECTED;
            } else {
                return v2;
            }
        } else {
            if (v1 == AgentStat.NOT_COLLECTED) {
                return v1;
            } else {
                return v1 + v2;
            }
        }
    }
    
    private static long maxValue(long v1, long v2) {
        if (v1 == AgentStat.NOT_COLLECTED) {
            return v2;
        } else if (v2 == AgentStat.NOT_COLLECTED) {
            return v1;
        }
        
        return v1 < v2 ? v2 : v1;
    }
}
