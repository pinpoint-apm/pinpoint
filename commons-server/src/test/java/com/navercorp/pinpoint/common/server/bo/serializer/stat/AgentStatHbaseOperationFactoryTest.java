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

import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecTestConfig;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.navercorp.pinpoint.common.hbase.HbaseColumnFamily.AGENT_STAT_STATISTICS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * @author HyunGil Jeong
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = CodecTestConfig.class)
public class AgentStatHbaseOperationFactoryTest {

    protected static final String TEST_AGENT_ID = "testAgentId";
    private static final AgentStatType TEST_AGENT_STAT_TYPE = AgentStatType.JVM_GC;
    protected static final long TEST_COLLECTION_INTERVAL = 5000L;

    @Mock
    private HbaseSerializer<List<AgentStatDataPoint>, Put> mockSerializer;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

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
        final long initialTimestamp = AGENT_STAT_STATISTICS.TIMESPAN_MS + 1L;
        final long expectedBaseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertThat(puts).hasSize(1);
        Put put = puts.get(0);
        assertPut(put, expectedBaseTimestamp);
    }

    @Test
    public void create_should_create_one_put_if_dataPoints_fit_into_a_single_slot() {
        // Given
        final int numDataPoints = 6;
        final long initialTimestamp = AGENT_STAT_STATISTICS.TIMESPAN_MS;
        final long expectedBaseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertThat(puts).hasSize(1);
        Put put = puts.get(0);
        assertPut(put, expectedBaseTimestamp);
    }

    @Test
    public void create_should_create_two_puts_if_dataPoints_span_over_a_timespan() {
        // Given
        final int numDataPoints = 6;
        final long initialTimestamp = AGENT_STAT_STATISTICS.TIMESPAN_MS - TEST_COLLECTION_INTERVAL;
        final long expectedBaseTimestamp1 = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final long expectedBaseTimestamp2 = AgentStatUtils.getBaseTimestamp(expectedBaseTimestamp1 + AGENT_STAT_STATISTICS.TIMESPAN_MS);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertThat(puts).hasSize(2);
        Put firstPut = puts.get(0);
        assertPut(firstPut, expectedBaseTimestamp1);
        Put secondPut = puts.get(1);
        assertPut(secondPut, expectedBaseTimestamp2);
    }

    private void assertPut(Put put, long expectedBaseTimestamp) {
        assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(put.getRow()));
        assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(put.getRow()));
        assertEquals(expectedBaseTimestamp, this.agentStatHbaseOperationFactory.getBaseTimestamp(put.getRow()));
    }

    @Test
    public void create_should_create_the_same_number_of_puts_as_dataPoints_if_collectionInterval_equals_timespan() {
        // Given
        final int numDataPoints = 100;
        final long initialTimestamp = AGENT_STAT_STATISTICS.TIMESPAN_MS - 1L;
        final long expectedInitialBaseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, AGENT_STAT_STATISTICS.TIMESPAN_MS, numDataPoints);
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertThat(puts).hasSize(numDataPoints);
        for (int i = 0; i < puts.size(); i++) {
            Put put = puts.get(i);
            assertEquals(TEST_AGENT_ID, this.agentStatHbaseOperationFactory.getAgentId(put.getRow()));
            assertEquals(TEST_AGENT_STAT_TYPE, this.agentStatHbaseOperationFactory.getAgentStatType(put.getRow()));
            long expectedBaseTimestamp = expectedInitialBaseTimestamp + (i * AGENT_STAT_STATISTICS.TIMESPAN_MS);
            assertEquals(expectedBaseTimestamp, this.agentStatHbaseOperationFactory.getBaseTimestamp(put.getRow()));
        }
    }

    @Test
    public void test_using_current_timestamp() {
        // Given
        final int numDataPoints = 6;
        final long initialTimestamp = System.currentTimeMillis() - (TEST_COLLECTION_INTERVAL * numDataPoints);
        final List<AgentStatDataPoint> testDataPoints = createTestDataPoints(initialTimestamp, TEST_COLLECTION_INTERVAL, numDataPoints);
        final Set<Long> uniqueTimeslots = new TreeSet<>();
        for (AgentStatDataPoint testDataPoint : testDataPoints) {
            uniqueTimeslots.add(AgentStatUtils.getBaseTimestamp(testDataPoint.getTimestamp()));
        }
        // When
        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, testDataPoints, this.mockSerializer);
        // Then
        assertThat(puts).hasSameSizeAs(uniqueTimeslots);
        int i = 0;
        for (Long timeslot : uniqueTimeslots) {
            long expectedBaseTimestamp = timeslot;
            Put put = puts.get(i++);
            assertPut(put, expectedBaseTimestamp);
        }
    }

    private List<AgentStatDataPoint> createTestDataPoints(long initialTimestamp, long interval, int count) {
        List<AgentStatDataPoint> dataPoints = new ArrayList<>(count);
        long timestamp = initialTimestamp;
        for (int i = 0; i < count; i++) {
            AgentStatDataPoint dataPoint = createTestDataPoint(timestamp);
            dataPoints.add(dataPoint);
            timestamp += interval;
        }
        return dataPoints;
    }

    private AgentStatDataPoint createTestDataPoint(final long testTimestamp) {
        final String testAgentId = "testAgentId";
        final long testStartTimestamp = 0L;

        AgentStatDataPoint mock = mock(AgentStatDataPoint.class);
        lenient().when(mock.getAgentId()).thenReturn(testAgentId);
        lenient().when(mock.getStartTimestamp()).thenReturn(testStartTimestamp);
        lenient().when(mock.getTimestamp()).thenReturn(testTimestamp);
        lenient().when(mock.getAgentStatType()).thenReturn(AgentStatType.UNKNOWN);
        return mock;
    }
}
