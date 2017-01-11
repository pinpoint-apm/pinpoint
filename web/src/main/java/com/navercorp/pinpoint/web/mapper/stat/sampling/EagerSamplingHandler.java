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

package com.navercorp.pinpoint.web.mapper.stat.sampling;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author HyunGil Jeong
 */
public class EagerSamplingHandler<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> implements AgentStatSamplingHandler<T, S> {

    private final TimeWindow timeWindow;
    private final AgentStatSampler<T, S> sampler;

    private final Map<Long, SamplingPartitionContext> samplingContexts = new HashMap<>();
    private final Map<Long, SortedMap<Long, S>> sampledPointProjection = new TreeMap<>();

    public EagerSamplingHandler(TimeWindow timeWindow, AgentStatSampler<T, S> sampler) {
        this.timeWindow = timeWindow;
        this.sampler = sampler;
    }

    public void addDataPoint(T dataPoint) {
        long startTimestamp = dataPoint.getStartTimestamp();
        long timestamp = dataPoint.getTimestamp();
        long timeslotTimestamp = timeWindow.refineTimestamp(timestamp);
        SamplingPartitionContext samplingContext = samplingContexts.get(startTimestamp);
        if (samplingContext == null) {
            samplingContext = new SamplingPartitionContext(timeslotTimestamp, dataPoint);
            samplingContexts.put(startTimestamp, samplingContext);
        } else {
            long timeslotTimestampToSample = samplingContext.getTimeslotTimestamp();
            if (timeslotTimestampToSample == timeslotTimestamp) {
                samplingContext.addDataPoint(dataPoint);
            } else if (timeslotTimestampToSample > timeslotTimestamp){
                S sampledPoint = samplingContext.sampleDataPoints(dataPoint);
                SortedMap<Long, S> sampledPoints = sampledPointProjection.get(timeslotTimestampToSample);
                if (sampledPoints == null) {
                    sampledPoints = new TreeMap<>();
                    sampledPointProjection.put(timeslotTimestampToSample, sampledPoints);
                }
                sampledPoints.put(startTimestamp, sampledPoint);
                samplingContext = new SamplingPartitionContext(timeslotTimestamp, dataPoint);
                samplingContexts.put(startTimestamp, samplingContext);
            } else {
                // Results should be sorted in a descending order of their actual timestamp values
                // as they are stored using reverse timestamp.
                throw new IllegalStateException("Out of order AgentStatDataPoint");
            }
        }
    }

    public List<S> getSampledDataPoints() {
        // sample remaining data point projections
        for (Map.Entry<Long, SamplingPartitionContext> e : samplingContexts.entrySet()) {
            long startTimestamp = e.getKey();
            SamplingPartitionContext samplingPartitionContext = e.getValue();
            long timeslotTimestamp = samplingPartitionContext.getTimeslotTimestamp();
            S sampledDataPoint = samplingPartitionContext.sampleDataPoints();
            SortedMap<Long, S> reduceCandidates = sampledPointProjection.get(timeslotTimestamp);
            if (reduceCandidates == null) {
                reduceCandidates = new TreeMap<>();
                sampledPointProjection.put(timeslotTimestamp, reduceCandidates);
            }
            reduceCandidates.put(startTimestamp, sampledDataPoint);
        }
        // reduce projection
        if (sampledPointProjection.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<S> sampledDataPoints = new ArrayList<>(sampledPointProjection.size());
            for (SortedMap<Long, S> sampledPointCandidates : sampledPointProjection.values()) {
                sampledDataPoints.add(reduceSampledPoints(sampledPointCandidates));
            }
            return sampledDataPoints;
        }
    }

    private S reduceSampledPoints(SortedMap<Long, S> sampledPointCandidates) {
        Long lastKey = sampledPointCandidates.lastKey();
        return sampledPointCandidates.get(lastKey);
    }

    private class SamplingPartitionContext {

        private final int timeslotIndex;
        private final long timeslotTimestamp;
        private final List<T> dataPoints = new ArrayList<>();

        private SamplingPartitionContext(long timeslotTimestamp, T initialDataPoint) {
            this.timeslotTimestamp = timeslotTimestamp;
            this.dataPoints.add(initialDataPoint);
            this.timeslotIndex = timeWindow.getWindowIndex(this.timeslotTimestamp);
        }

        private void addDataPoint(T dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        private long getTimeslotTimestamp() {
            return timeslotTimestamp;
        }

        private S sampleDataPoints() {
            return sampleDataPoints(null);
        }

        private S sampleDataPoints(T previousDataPoint) {
            return sampler.sampleDataPoints(timeslotIndex, timeslotTimestamp, dataPoints, previousDataPoint);
        }

    }
}
