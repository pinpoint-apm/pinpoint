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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import org.apache.hadoop.hbase.client.Put;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.AGENT_STAT_TIMESPAN_MS;
import static org.junit.Assert.assertEquals;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class AgentStatHbaseOperationFactoryTest {

    protected static final String TEST_AGENT_ID = "testAgentId";
    private static final AgentStatType TEST_AGENT_STAT_TYPE = AgentStatType.JVM_GC;
    protected static final long TEST_COLLECTION_INTERVAL = 5000L;

    @Mock
    private HbaseSerializer<List<AgentStatDataPoint>, Put> mockSerializer;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void create_should_return_empty_list_for_null_dataPoints() {
        List<AgentStatDataPoint> dataPoints = null;
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, dataPoints, this.mockSerializer);
        assertEquals(Collections.<Put>emptyList(), puts);
    }

    @Test
    public void create_should_return_empty_list_for_empty_dataPoints() {
        List<AgentStatDataPoint> dataPoints = Collections.emptyList();
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, dataPoints, this.mockSerializer);
        assertEquals(Collections.<Put>emptyList(), puts);
    }

    @Test
    public void create_should_create_one_put_if_there_is_only_one_dataPoint() {
        // Given
        final int numDataPoints = 1;
        final long initialTimestamp = AGENT_STAT_TIMESPAN_MS + 1L;
        final long expectedBaseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertEquals(1, puts.size());
        Put put = puts.get(0);
        assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(put.getRow()));
        assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(put.getRow()));
        assertEquals(expectedBaseTimestamp, this.agentStatHbaseOperationFactory.getBaseTimestamp(put.getRow()));
    }

    @Test
    public void create_should_create_one_put_if_dataPoints_fit_into_a_single_slot() {
        // Given
        final int numDataPoints = 6;
        final long initialTimestamp = AGENT_STAT_TIMESPAN_MS;
        final long expectedBaseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertEquals(1, puts.size());
        Put put = puts.get(0);
        assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(put.getRow()));
        assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(put.getRow()));
        assertEquals(expectedBaseTimestamp, this.agentStatHbaseOperationFactory.getBaseTimestamp(put.getRow()));
    }

    @Test
    public void create_should_create_two_puts_if_dataPoints_span_over_a_timespan() {
        // Given
        final int numDataPoints = 6;
        final long initialTimestamp = AGENT_STAT_TIMESPAN_MS - TEST_COLLECTION_INTERVAL;
        final long expectedBaseTimestamp1 = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final long expectedBaseTimestamp2 = AgentStatUtils.getBaseTimestamp(expectedBaseTimestamp1 + AGENT_STAT_TIMESPAN_MS);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertEquals(2, puts.size());
        Put firstPut = puts.get(0);
        assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(firstPut.getRow()));
        assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(firstPut.getRow()));
        assertEquals(expectedBaseTimestamp1, this.agentStatHbaseOperationFactory.getBaseTimestamp(firstPut.getRow()));
        Put secondPut = puts.get(1);
        assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(secondPut.getRow()));
        assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(secondPut.getRow()));
        assertEquals(expectedBaseTimestamp2, this.agentStatHbaseOperationFactory.getBaseTimestamp(secondPut.getRow()));
    }

    @Test
    public void create_should_create_the_same_number_of_puts_as_dataPoints_if_collectionInterval_equals_timespan() {
        // Given
        final int numDataPoints = 100;
        final long initialTimestamp = AGENT_STAT_TIMESPAN_MS - 1L;
        final long expectedInitialBaseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, AGENT_STAT_TIMESPAN_MS, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertEquals(numDataPoints, puts.size());
        for (int i = 0; i < puts.size(); ++i) {
            Put put = puts.get(i);
            assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(put.getRow()));
            assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(put.getRow()));
            long expectedBaseTimestamp = expectedInitialBaseTimestamp + (i * AGENT_STAT_TIMESPAN_MS);
            assertEquals(expectedBaseTimestamp, this.agentStatHbaseOperationFactory.getBaseTimestamp(put.getRow()));
        }
    }

    @Test
    public void test_using_current_timestamp() {
        // Given
        final int numDataPoints = 6;
        final long initialTimestamp = System.currentTimeMillis() - (TEST_COLLECTION_INTERVAL * numDataPoints);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        final Set<Long> uniqueTimeslots = new TreeSet<Long>();
        for (AgentStatDataPoint testDataPoint : testDataPoints) {
            uniqueTimeslots.add(AgentStatUtils.getBaseTimestamp(testDataPoint.getTimestamp()));
        }
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertEquals(uniqueTimeslots.size(), puts.size());
        int i = 0;
        for (Long timeslot : uniqueTimeslots) {
            long expectedBaseTimestamp = timeslot;
            Put put = puts.get(i++);
            assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(put.getRow()));
            assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(put.getRow()));
            assertEquals(expectedBaseTimestamp, this.agentStatHbaseOperationFactory.getBaseTimestamp(put.getRow()));
        }
    }

    private List<AgentStatDataPoint> createTestDataPoints(long initialTimestamp, long interval, int count) {
        List<AgentStatDataPoint> dataPoints = new ArrayList<AgentStatDataPoint>(count);
        long timestamp = initialTimestamp;
        for (int i = 0; i < count; ++i) {
            AgentStatDataPoint dataPoint = createTestDataPoint(timestamp);
            dataPoints.add(dataPoint);
            timestamp += interval;
        }
        return dataPoints;
    }

    private AgentStatDataPoint createTestDataPoint(final long testTimestamp) {
        final String testAgentId = "testAgentId";
        final long testStartTimestamp = 0L;
        return new AgentStatDataPoint() {
            private String agentId = testAgentId;
            private long startTimestamp = testStartTimestamp;
            private long timestamp = testTimestamp;

            @Override
            public String getAgentId() {
                return this.agentId;
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
                return this.timestamp;
            }
            @Override
            public void setTimestamp(long timestamp) {
                this.timestamp = timestamp;
            }

            @Override
            public AgentStatType getAgentStatType() {
                return AgentStatType.UNKNOWN;
            }
        };
    }
}
