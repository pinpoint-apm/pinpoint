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
public class EagerSamplingHandler<IN extends JoinStatBo, OUT extends AggregationStatData> implements ApplicationStatSamplingHandler<IN, OUT> {

    private final TimeWindow timeWindow;
    private final ApplicationStatSampler<IN, OUT> sampler;

    private final Map<String, SamplingPartitionContext<IN, OUT>> samplingContexts = new HashMap<>();
    private final Map<Long, SortedMap<String, OUT>> sampledPointProjection = new TreeMap<>();

    public EagerSamplingHandler(TimeWindow timeWindow, ApplicationStatSampler<IN, OUT> sampler) {
        this.timeWindow = timeWindow;
        this.sampler = sampler;
    }

    public void addDataPoint(IN dataPoint) {
        String id = dataPoint.getId();
        long timestamp = dataPoint.getTimestamp();
        long timeslotTimestamp = timeWindow.refineTimestamp(timestamp);
        SamplingPartitionContext<IN, OUT> samplingContext = samplingContexts.get(id);
        if (samplingContext == null) {
            samplingContext = new SamplingPartitionContext<>(timeslotTimestamp, dataPoint, timeWindow, sampler);
            samplingContexts.put(id, samplingContext);
        } else {
            long timeslotTimestampToSample = samplingContext.getTimeslotTimestamp();
            if (timeslotTimestampToSample == timeslotTimestamp) {
                samplingContext.addDataPoint(dataPoint);
            } else if (timeslotTimestampToSample > timeslotTimestamp) {
                OUT sampledPoint = samplingContext.sampleDataPoints(dataPoint);
                SortedMap<String, OUT> sampledPoints = sampledPointProjection.computeIfAbsent(timeslotTimestampToSample, k -> new TreeMap<>());
                sampledPoints.put(id, sampledPoint);
                samplingContext = new SamplingPartitionContext<>(timeslotTimestamp, dataPoint, timeWindow, sampler);
                samplingContexts.put(id, samplingContext);
            } else {
                // Results should be sorted in a descending order of their actual timestamp values
                // as they are stored using reverse timestamp.
                throw new IllegalStateException("Out of order AgentStatDataPoint");
            }
        }
    }

    public List<OUT> getSampledDataPoints() {
        // sample remaining data point projections
        for (Map.Entry<String, SamplingPartitionContext<IN, OUT>> e : samplingContexts.entrySet()) {
            String id = e.getKey();
            SamplingPartitionContext<IN, OUT> samplingPartitionContext = e.getValue();
            long timeslotTimestamp = samplingPartitionContext.getTimeslotTimestamp();
            OUT sampledDataPoint = samplingPartitionContext.sampleDataPoints();
            SortedMap<String, OUT> reduceCandidates = sampledPointProjection.computeIfAbsent(timeslotTimestamp, k -> new TreeMap<>());
            reduceCandidates.put(id, sampledDataPoint);
        }
        // reduce projection
        if (sampledPointProjection.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<OUT> sampledDataPoints = new ArrayList<>(sampledPointProjection.size());
            for (SortedMap<String, OUT> sampledPointCandidates : sampledPointProjection.values()) {
                sampledDataPoints.add(reduceSampledPoints(sampledPointCandidates));
            }
            return sampledDataPoints;
        }
    }

    private OUT reduceSampledPoints(SortedMap<String, OUT> sampledPointCandidates) {
        String lastKey = sampledPointCandidates.lastKey();
        return sampledPointCandidates.get(lastKey);
    }

    private static class SamplingPartitionContext<IN extends JoinStatBo, OUT extends AggregationStatData> {

        private final int timeslotIndex;
        private final long timeslotTimestamp;
        private final List<IN> dataPoints = new ArrayList<>();
        private final ApplicationStatSampler<IN, OUT> sampler;

        private SamplingPartitionContext(long timeslotTimestamp, IN initialDataPoint,
                                         TimeWindow timeWindow, ApplicationStatSampler<IN, OUT> sampler) {
            this.timeslotTimestamp = timeslotTimestamp;
            this.dataPoints.add(initialDataPoint);
            this.timeslotIndex = timeWindow.getWindowIndex(this.timeslotTimestamp);
            this.sampler = sampler;
        }

        private void addDataPoint(IN dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        private long getTimeslotTimestamp() {
            return timeslotTimestamp;
        }

        private OUT sampleDataPoints() {
            return sampleDataPoints(null);
        }

        private OUT sampleDataPoints(IN previousDataPoint) {
            return sampler.sampleDataPoints(timeslotIndex, timeslotTimestamp, dataPoints, previousDataPoint);
        }

    }
}