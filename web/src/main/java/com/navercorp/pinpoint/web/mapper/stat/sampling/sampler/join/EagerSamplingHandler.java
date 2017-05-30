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
package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.join;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSamplingHandler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;

import java.util.*;

/**
 * @author minwoo.jung
 */
public class EagerSamplingHandler implements ApplicationStatSamplingHandler {

    private final TimeWindow timeWindow;
    private final ApplicationStatSampler sampler;

    private final Map<String, SamplingPartitionContext> samplingContexts = new HashMap<>();
    private final Map<Long, SortedMap<String, AggregationStatData>> sampledPointProjection = new TreeMap<>();

    public EagerSamplingHandler(TimeWindow timeWindow, ApplicationStatSampler sampler) {
        this.timeWindow = timeWindow;
        this.sampler = sampler;
    }

    public void addDataPoint(JoinStatBo dataPoint) {
        String id = dataPoint.getId();
        long timestamp = dataPoint.getTimestamp();
        long timeslotTimestamp = timeWindow.refineTimestamp(timestamp);
        SamplingPartitionContext samplingContext = samplingContexts.get(id);
        if (samplingContext == null) {
            samplingContext = new SamplingPartitionContext(timeslotTimestamp, dataPoint);
            samplingContexts.put(id, samplingContext);
        } else {
            long timeslotTimestampToSample = samplingContext.getTimeslotTimestamp();
            if (timeslotTimestampToSample == timeslotTimestamp) {
                samplingContext.addDataPoint(dataPoint);
            } else if (timeslotTimestampToSample > timeslotTimestamp) {
                AggregationStatData sampledPoint = samplingContext.sampleDataPoints(dataPoint);
                SortedMap<String, AggregationStatData> sampledPoints = sampledPointProjection.get(timeslotTimestampToSample);
                if (sampledPoints == null) {
                    sampledPoints = new TreeMap<>();
                    sampledPointProjection.put(timeslotTimestampToSample, sampledPoints);
                }
                sampledPoints.put(id, sampledPoint);
                samplingContext = new SamplingPartitionContext(timeslotTimestamp, dataPoint);
                samplingContexts.put(id, samplingContext);
            } else {
                // Results should be sorted in a descending order of their actual timestamp values
                // as they are stored using reverse timestamp.
                throw new IllegalStateException("Out of order AgentStatDataPoint");
            }
        }
    }

    public List<AggregationStatData> getSampledDataPoints() {
        // sample remaining data point projections
        for (Map.Entry<String, SamplingPartitionContext> e : samplingContexts.entrySet()) {
            String id = e.getKey();
            SamplingPartitionContext samplingPartitionContext = e.getValue();
            long timeslotTimestamp = samplingPartitionContext.getTimeslotTimestamp();
            AggregationStatData sampledDataPoint = samplingPartitionContext.sampleDataPoints();
            SortedMap<String, AggregationStatData> reduceCandidates = sampledPointProjection.get(timeslotTimestamp);
            if (reduceCandidates == null) {
                reduceCandidates = new TreeMap<>();
                sampledPointProjection.put(timeslotTimestamp, reduceCandidates);
            }
            reduceCandidates.put(id, sampledDataPoint);
        }
        // reduce projection
        if (sampledPointProjection.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<AggregationStatData> sampledDataPoints = new ArrayList<>(sampledPointProjection.size());
            for (SortedMap<String, AggregationStatData> sampledPointCandidates : sampledPointProjection.values()) {
                sampledDataPoints.add(reduceSampledPoints(sampledPointCandidates));
            }
            return sampledDataPoints;
        }
    }

    private AggregationStatData reduceSampledPoints(SortedMap<String, AggregationStatData> sampledPointCandidates) {
        String lastKey = sampledPointCandidates.lastKey();
        return sampledPointCandidates.get(lastKey);
    }

    private class SamplingPartitionContext {

        private final int timeslotIndex;
        private final long timeslotTimestamp;
        private final List<JoinStatBo> dataPoints = new ArrayList<>();

        private SamplingPartitionContext(long timeslotTimestamp, JoinStatBo initialDataPoint) {
            this.timeslotTimestamp = timeslotTimestamp;
            this.dataPoints.add(initialDataPoint);
            this.timeslotIndex = timeWindow.getWindowIndex(this.timeslotTimestamp);
        }

        private void addDataPoint(JoinStatBo dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        private long getTimeslotTimestamp() {
            return timeslotTimestamp;
        }

        private AggregationStatData sampleDataPoints() {
            return sampleDataPoints(null);
        }

        private AggregationStatData sampleDataPoints(JoinStatBo previousDataPoint) {
            return sampler.sampleDataPoints(timeslotIndex, timeslotTimestamp, dataPoints, previousDataPoint);
        }

    }
}