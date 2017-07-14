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
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class SampledAgentStatResultExtractorTest {

    private static final long DEFAULT_TIME_INTERVAL = 5 * 1000L;

    private static final TimeWindowSampler ONE_TO_ONE_SAMPLER = new TimeWindowSampler() {
        @Override
        public long getWindowSize(Range range) {
            return DEFAULT_TIME_INTERVAL;
        }
    };

    private static final TimeWindowSampler TWO_TO_ONE_SAMPLER = new TimeWindowSampler() {
        @Override
        public long getWindowSize(Range range) {
            return DEFAULT_TIME_INTERVAL * 2;
        }
    };

    private static final TimeWindowSampler TEN_TO_ONE_SAMPLER = new TimeWindowSampler() {
        @Override
        public long getWindowSize(Range range) {
            return DEFAULT_TIME_INTERVAL * 10;
        }
    };

    @Mock
    private ResultScanner resultScanner;

    @Mock
    private Result result;

    @Mock
    private AgentStatMapperV2<TestAgentStatDataPoint> rowMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this.getClass());
        when(this.resultScanner.iterator()).thenReturn(Arrays.asList(this.result).iterator());
    }

    @Test
    public void one_to_one_sampler_should_not_down_sample_data_points() throws Exception {
        // Given
        final int numValues = 10;
        final long initialTimestamp = System.currentTimeMillis();
        final long finalTimestamp = initialTimestamp + (DEFAULT_TIME_INTERVAL * numValues);
        final TimeWindow timeWindow = new TimeWindow(new Range(initialTimestamp, finalTimestamp), ONE_TO_ONE_SAMPLER);
        final List<TestAgentStatDataPoint> dataPoints = createDataPoints(finalTimestamp, DEFAULT_TIME_INTERVAL, numValues);
        final Map<Long, List<TestAgentStatDataPoint>> expectedDataPointSlotMap = getExpectedDataPointSlotMap(timeWindow, dataPoints);
        when(this.rowMapper.mapRow(this.result, 0)).thenReturn(dataPoints);

        TestAgentStatSampler testAgentStatSampler = new TestAgentStatSampler();
        SampledAgentStatResultExtractor<TestAgentStatDataPoint, TestSampledAgentStatDataPoint> resultExtractor
                = new SampledAgentStatResultExtractor<>(timeWindow, this.rowMapper, testAgentStatSampler);
        // When
        List<TestSampledAgentStatDataPoint> sampledDataPoints = resultExtractor.extractData(this.resultScanner);
        // Then
        for (TestSampledAgentStatDataPoint sampledDataPoint : sampledDataPoints) {
            List<TestAgentStatDataPoint> expectedSampledDataPoints = expectedDataPointSlotMap.get(sampledDataPoint.getBaseTimestamp());
            Assert.assertEquals(expectedSampledDataPoints, sampledDataPoint.getDataPointsToSample());
        }
    }

    @Test
    public void two_to_one_sample_should_down_sample_correctly() throws Exception {
        // Given
        final int numValues = 20;
        final long initialTimestamp = System.currentTimeMillis();
        final long finalTimestamp = initialTimestamp + (DEFAULT_TIME_INTERVAL * numValues);
        final TimeWindow timeWindow = new TimeWindow(new Range(initialTimestamp, finalTimestamp), TWO_TO_ONE_SAMPLER);
        final List<TestAgentStatDataPoint> dataPoints = createDataPoints(finalTimestamp, DEFAULT_TIME_INTERVAL, numValues);
        final Map<Long, List<TestAgentStatDataPoint>> expectedDataPointSlotMap = getExpectedDataPointSlotMap(timeWindow, dataPoints);
        when(this.rowMapper.mapRow(this.result, 0)).thenReturn(dataPoints);

        TestAgentStatSampler testAgentStatSampler = new TestAgentStatSampler();
        SampledAgentStatResultExtractor<TestAgentStatDataPoint, TestSampledAgentStatDataPoint> resultExtractor
                = new SampledAgentStatResultExtractor<>(timeWindow, this.rowMapper, testAgentStatSampler);
        // When
        List<TestSampledAgentStatDataPoint> sampledDataPoints = resultExtractor.extractData(this.resultScanner);
        // Then
        for (TestSampledAgentStatDataPoint sampledDataPoint : sampledDataPoints) {
            List<TestAgentStatDataPoint> expectedSampledDataPoints = expectedDataPointSlotMap.get(sampledDataPoint.getBaseTimestamp());
            Assert.assertEquals(expectedSampledDataPoints, sampledDataPoint.getDataPointsToSample());
        }
    }

    @Test
    public void ten_to_one_sample_should_down_sample_correctly() throws Exception {
        // Given
        final int numValues = 100;
        final long initialTimestamp = System.currentTimeMillis();
        final long finalTimestamp = initialTimestamp + (DEFAULT_TIME_INTERVAL * numValues);
        final TimeWindow timeWindow = new TimeWindow(new Range(initialTimestamp, finalTimestamp), TEN_TO_ONE_SAMPLER);
        final List<TestAgentStatDataPoint> dataPoints = createDataPoints(finalTimestamp, DEFAULT_TIME_INTERVAL, numValues);
        final Map<Long, List<TestAgentStatDataPoint>> expectedDataPointSlotMap = getExpectedDataPointSlotMap(timeWindow, dataPoints);
        when(this.rowMapper.mapRow(this.result, 0)).thenReturn(dataPoints);

        TestAgentStatSampler testAgentStatSampler = new TestAgentStatSampler();
        SampledAgentStatResultExtractor<TestAgentStatDataPoint, TestSampledAgentStatDataPoint> resultExtractor
                = new SampledAgentStatResultExtractor<>(timeWindow, this.rowMapper, testAgentStatSampler);
        // When
        List<TestSampledAgentStatDataPoint> sampledDataPoints = resultExtractor.extractData(this.resultScanner);
        // Then
        for (TestSampledAgentStatDataPoint sampledDataPoint : sampledDataPoints) {
            List<TestAgentStatDataPoint> expectedSampledDataPoints = expectedDataPointSlotMap.get(sampledDataPoint.getBaseTimestamp());
            Assert.assertEquals(expectedSampledDataPoints, sampledDataPoint.getDataPointsToSample());
        }
    }

    private Map<Long, List<TestAgentStatDataPoint>> getExpectedDataPointSlotMap(TimeWindow timeWindow, List<TestAgentStatDataPoint> dataPoints) {
        Map<Long, List<TestAgentStatDataPoint>> slotMap = new HashMap<>();
        for (long timeslotTimestamp : timeWindow) {
            slotMap.put(timeslotTimestamp, new ArrayList<TestAgentStatDataPoint>());
        }
        for (TestAgentStatDataPoint dataPoint : dataPoints) {
            slotMap.get(timeWindow.refineTimestamp(dataPoint.getTimestamp())).add(dataPoint);
        }
        return slotMap;
    }

    private List<TestAgentStatDataPoint> createDataPoints(long finalTimestamp, long timeInterval, int numDataPoints) {
        List<TestAgentStatDataPoint> dataPoints = new ArrayList<>(numDataPoints);
        for (int i = 0; i < numDataPoints; ++i) {
            TestAgentStatDataPoint dataPoint = new TestAgentStatDataPoint();
            dataPoint.setTimestamp(finalTimestamp - (timeInterval * i));
            dataPoint.setValue(i);
            dataPoints.add(dataPoint);
        }
        return dataPoints;
    }

    private static class TestAgentStatSampler implements AgentStatSampler<TestAgentStatDataPoint, TestSampledAgentStatDataPoint> {

        @Override
        public TestSampledAgentStatDataPoint sampleDataPoints(int timeWindowIndex, long timestamp, List<TestAgentStatDataPoint> dataPoints, TestAgentStatDataPoint previousDataPoint) {
            return new TestSampledAgentStatDataPoint(timestamp, dataPoints);
        }
    }

    private static class TestAgentStatDataPoint implements AgentStatDataPoint {
        private String agentId;
        private long startTimestamp;
        private long timestamp;
        private int value;

        @Override
        public String getAgentId() {
            return agentId;
        }

        @Override
        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        @Override
        public long getStartTimestamp() {
            return startTimestamp;
        }

        @Override
        public void setStartTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public AgentStatType getAgentStatType() {
            return AgentStatType.UNKNOWN;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestAgentStatDataPoint{" +
                    "agentId='" + agentId + '\'' +
                    ", startTimestamp=" + startTimestamp +
                    ", timestamp=" + timestamp +
                    ", value=" + value +
                    '}';
        }
    }

    private static class TestSampledAgentStatDataPoint implements SampledAgentStatDataPoint {
        private final long baseTimestamp;
        private final List<TestAgentStatDataPoint> dataPointsToSample;

        private TestSampledAgentStatDataPoint(long baseTimestamp, List<TestAgentStatDataPoint> dataPointsToSample) {
            this.baseTimestamp = baseTimestamp;
            this.dataPointsToSample = dataPointsToSample;
        }

        public long getBaseTimestamp() {
            return baseTimestamp;
        }

        public List<TestAgentStatDataPoint> getDataPointsToSample() {
            return dataPointsToSample;
        }

        @Override
        public String toString() {
            return "TestSampledAgentStatDataPoint{" +
                    "baseTimestamp=" + baseTimestamp +
                    ", dataPointsToSample=" + dataPointsToSample +
                    '}';
        }
    }
}
