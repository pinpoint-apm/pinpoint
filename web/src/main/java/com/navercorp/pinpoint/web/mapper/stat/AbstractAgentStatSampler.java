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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author HyunGil Jeong
 */
public abstract class AbstractAgentStatSampler<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> implements AgentStatSampler<T, S> {

    private static final int INITIAL_TIME_WINDOW_INDEX = -1;

    @Override
    public final List<S> sampleDataPoints(TimeWindow timeWindow, List<T> dataPoints) {
        Map<Long, List<T>> dataPointPartitions = partitionDataPoints(dataPoints);

        Map<Long, SortedMap<Long, S>> sampledPointProjection = mapProjection(timeWindow, dataPointPartitions);

        List<S> sampledDataPoints = new ArrayList<>(sampledPointProjection.size());
        for (SortedMap<Long, S> sampledPointCandidates : sampledPointProjection.values()) {
            sampledDataPoints.add(reduceSampledPoints(sampledPointCandidates));
        }
        return sampledDataPoints;
    }

    /**
     * Returns a map of data points partitioned by the start timestamp of the agent. This is mainly to distinguish
     * between different agent life cycles, and prevent stats from being mixed up when there are multiple agents with
     * the same agent id.
     *
     * @param dataPoints a list of data points to partition
     * @return a map of data points partitioned by agent start timestamps
     */
    private Map<Long, List<T>> partitionDataPoints(List<T> dataPoints) {
        Map<Long, List<T>> dataPointPartitions = new HashMap<>();
        for (T jvmGcBo : dataPoints) {
            long startTimestamp = jvmGcBo.getStartTimestamp();
            List<T> dataPointPartition = dataPointPartitions.get(startTimestamp);
            if (dataPointPartition == null) {
                dataPointPartition = new ArrayList<>(dataPoints.size());
                dataPointPartitions.put(startTimestamp, dataPointPartition);
            }
            dataPointPartition.add(jvmGcBo);
        }
        return dataPointPartitions;
    }

    /**
     * Collapses multiple projections of sampled data points into a single map with key sorted by timeslot timestamp,
     * and values being sampled data points of different agent life cycles mapped by their agent start timestamp.
     *
     * @param timeWindow the TimeWindow used to create the timeslot map
     * @param dataPointPartitions a map of data points partitioned by their agent start timestamp
     * @return a map of timeslots with sampled data points mapped by their agent start timestamp as values
     */
    private Map<Long, SortedMap<Long, S>> mapProjection(TimeWindow timeWindow, Map<Long, List<T>> dataPointPartitions) {
        Map<Long, SortedMap<Long, S>> sampledPointProjection = new TreeMap<>();
        for (Map.Entry<Long, List<T>> dataPointPartitionEntry : dataPointPartitions.entrySet()) {
            Long startTimestamp = dataPointPartitionEntry.getKey();
            List<T> dataPointPartition = dataPointPartitionEntry.getValue();
            Map<Long, S> sampledDataPointPartition = sampleDataPointPartition(timeWindow, dataPointPartition);

            for (Map.Entry<Long, S> e : sampledDataPointPartition.entrySet()) {
                Long timeslotTimestamp = e.getKey();
                S sampledDataPoint = e.getValue();
                SortedMap<Long, S> timeslotSampleEntry = sampledPointProjection.get(timeslotTimestamp);
                if (timeslotSampleEntry == null) {
                    timeslotSampleEntry = new TreeMap<>();
                    sampledPointProjection.put(timeslotTimestamp, timeslotSampleEntry);
                }
                timeslotSampleEntry.put(startTimestamp, sampledDataPoint);
            }
        }
        return sampledPointProjection;
    }

    /**
     * Returns a map of timeslot timestamps with sampled data points as value.
     *
     * @param timeWindow the TimeWindow used to create the timeslot map
     * @param dataPoints a list of data points to sample
     * @return a map of timeslots with sampled data points as values
     */
    private Map<Long, S> sampleDataPointPartition(TimeWindow timeWindow, List<T> dataPoints) {
        Map<Long, S> sampledDataPoints = new HashMap<>((int) timeWindow.getWindowRangeCount());
        T previous;
        List<T> currentBatch = new ArrayList<>();
        int currentTimeWindowIndex = INITIAL_TIME_WINDOW_INDEX;
        long currentTimeslotTimestamp = 0;
        for (T dataPoint : dataPoints) {
            long timestamp = dataPoint.getTimestamp();
            int timeWindowIndex = timeWindow.getWindowIndex(timestamp);
            if (currentTimeWindowIndex == INITIAL_TIME_WINDOW_INDEX || currentTimeWindowIndex == timeWindowIndex) {
                currentBatch.add(dataPoint);
            } else if (timeWindowIndex < currentTimeWindowIndex) {
                previous = dataPoint;
                // currentBatch shouldn't be empty at this point
                S sampledBatch = sampleDataPoints(currentTimeWindowIndex, currentTimeslotTimestamp, currentBatch, previous);
                sampledDataPoints.put(currentTimeslotTimestamp, sampledBatch);
                currentBatch = new ArrayList<>(currentBatch.size());
                currentBatch.add(dataPoint);
            } else {
                // Results should be sorted in a descending order of their actual timestamp values
                // as they are stored using reverse timestamp.
                throw new IllegalStateException("Out of order AgentStatDataPoint");
            }
            currentTimeslotTimestamp = timeWindow.refineTimestamp(timestamp);
            currentTimeWindowIndex = timeWindowIndex;
        }
        if (!currentBatch.isEmpty()) {
            S sampledBatch = sampleDataPoints(currentTimeWindowIndex, currentTimeslotTimestamp, currentBatch, null);
            sampledDataPoints.put(currentTimeslotTimestamp, sampledBatch);
        }
        return sampledDataPoints;
    }

    /**
     * Returns the sampled data point of the most recently started agent out of multiple candidates.
     *
     * @param sampledPointCandidates a sorted map of sampled data points in ascending order of agent start timestamp
     * @return sampled data point of the most recently started agent
     */
    protected S reduceSampledPoints(SortedMap<Long, S> sampledPointCandidates) {
        Long lastKey = sampledPointCandidates.lastKey();
        return sampledPointCandidates.get(lastKey);
    }

    protected abstract S sampleDataPoints(int timeWindowIndex, long timestamp, List<T> dataPoints, T previousDataPoint);
}
