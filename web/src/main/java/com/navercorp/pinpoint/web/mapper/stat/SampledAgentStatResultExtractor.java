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

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.ArrayList;
import java.util.List;


/**
 * @author HyunGil Jeong
 */
public abstract class SampledAgentStatResultExtractor<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> implements ResultsExtractor<List<S>> {

    private final TimeWindow timeWindow;
    private final AgentStatMapper<T> rowMapper;
    private final List<S> sampledDataPoints;

    public SampledAgentStatResultExtractor(TimeWindow timeWindow, AgentStatMapper<T> rowMapper) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        this.rowMapper = rowMapper;
        this.sampledDataPoints = new ArrayList<>((int) timeWindow.getWindowRangeCount());
    }

    @Override
    public List<S> extractData(ResultScanner results) throws Exception {
        int rowNum = 0;
        // Sample straight away, tossing out already sampled data points so they can be garbage collected
        // as soon as possible.
        // This is mainly important when querying over a long period of time which simply using SampledChartBuilder
        // could could consume too much memory.
        List<T> currentBatchToSample = new ArrayList<>();
        long currentTimeslotTimestamp = 0;
        for (Result result : results) {
            List<T> dataPoints = this.rowMapper.mapRow(result, rowNum++);
            for (T dataPoint : dataPoints) {
                long timestamp = dataPoint.getTimestamp();
                long timeslotTimestamp = this.timeWindow.refineTimestamp(timestamp);
                if (currentTimeslotTimestamp == 0 || currentTimeslotTimestamp == timeslotTimestamp) {
                    currentBatchToSample.add(dataPoint);
                    currentTimeslotTimestamp = timeslotTimestamp;
                } else if (timeslotTimestamp < currentTimeslotTimestamp) {
                    // currentBatchToSample shouldn't be empty at this point
                    S sampledBatch = sampleCurrentBatch(currentTimeslotTimestamp, currentBatchToSample);
                    this.sampledDataPoints.add(sampledBatch);
                    currentBatchToSample = new ArrayList<>();
                    currentBatchToSample.add(dataPoint);
                    currentTimeslotTimestamp = timeslotTimestamp;
                } else {
                    // Results should be sorted in a descending order of their actual timestamp values
                    // as they are stored using reverse timestamp.
                    throw new IllegalStateException("Out of order AgentStatDataPoint");
                }
            }
        }
        if (!currentBatchToSample.isEmpty()) {
            S sampledBatch = sampleCurrentBatch(currentTimeslotTimestamp, currentBatchToSample);
            sampledDataPoints.add(sampledBatch);
        }
        return this.sampledDataPoints;
    }

    protected abstract S sampleCurrentBatch(long timestamp, List<T> dataPointsToSample);

}
