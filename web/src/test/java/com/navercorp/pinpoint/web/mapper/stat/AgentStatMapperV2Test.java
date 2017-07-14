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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatRowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatRowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.lang3.RandomUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public class AgentStatMapperV2Test {

    private static final int MAX_NUM_TEST_VALUES = 10 + 1; // Random API's upper bound field is exclusive

    private static final String AGENT_ID = "testAgent";
    private static final AgentStatType AGENT_STAT_TYPE = AgentStatType.UNKNOWN;
    private static final long COLLECT_INVERVAL = 5000L;
    private static final Random RANDOM = new Random();
    private static final TimestampFilter TEST_FILTER = new TimestampFilter() {
        @Override
        public boolean filter(long timestamp) {
            return false;
        }
    };

    private final AgentStatRowKeyEncoder rowKeyEncoder = new AgentStatRowKeyEncoder();

    private final AgentStatRowKeyDecoder rowKeyDecoder = new AgentStatRowKeyDecoder();

    private final AbstractRowKeyDistributor rowKeyDistributor = new RowKeyDistributorByHashPrefix(
            new RangeOneByteSimpleHash(0, 33, 64));

    private final AgentStatHbaseOperationFactory hbaseOperationFactory = new AgentStatHbaseOperationFactory(
            this.rowKeyEncoder, this.rowKeyDecoder, this.rowKeyDistributor);

    private final AgentStatCodec<TestAgentStat> codec = new TestAgentStatCodec();

    private final AgentStatEncoder<TestAgentStat> encoder = new TestAgentStatEncoder(this.codec);

    private final AgentStatDecoder<TestAgentStat> decoder = new TestAgentStatDecoder(this.codec);

    private final AgentStatSerializer<TestAgentStat> serializer = new TestAgentStatSerializer(this.encoder);

    @Test
    public void mapperTest() throws Exception {
        // Given
        List<TestAgentStat> givenAgentStats = new ArrayList<>();
        List<Put> puts = new ArrayList<>();
        long initialTimestamp = System.currentTimeMillis();
        int numBatch = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
        for (int i = 0; i < numBatch; ++i) {
            int batchSize = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
            List<TestAgentStat> agentStatBatch = createAgentStats(initialTimestamp, COLLECT_INVERVAL, batchSize);
            givenAgentStats.addAll(agentStatBatch);
            puts.addAll(this.hbaseOperationFactory.createPuts(AGENT_ID, AGENT_STAT_TYPE, agentStatBatch, this.serializer));
            initialTimestamp += batchSize * COLLECT_INVERVAL;
        }
        List<Cell> cellsToPut = new ArrayList<>();
        for (Put put : puts) {
            List<Cell> cells = put.getFamilyCellMap().get(HBaseTables.AGENT_STAT_CF_STATISTICS);
            cellsToPut.addAll(cells);
        }
        Result result = Result.create(cellsToPut);

        // When
        AgentStatMapperV2<TestAgentStat> mapper = new AgentStatMapperV2<>(this.hbaseOperationFactory, this.decoder, TEST_FILTER);
        List<TestAgentStat> mappedAgentStats = mapper.mapRow(result, 0);

        // Then
        Collections.sort(givenAgentStats, AgentStatMapperV2.REVERSE_TIMESTAMP_COMPARATOR);
        Assert.assertEquals(givenAgentStats, mappedAgentStats);
    }

    private List<TestAgentStat> createAgentStats(long initialTimestamp, long interval, int batchSize) {
        List<TestAgentStat> agentStats = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; ++i) {
            long timestamp = initialTimestamp + (interval * i);
            TestAgentStat agentStat = new TestAgentStat();
            agentStat.setAgentId(AGENT_ID);
            agentStat.setTimestamp(timestamp);
            agentStat.setValue(RANDOM.nextLong());
            agentStats.add(agentStat);
        }
        return agentStats;
    }

    private static class TestAgentStatCodec implements AgentStatCodec<TestAgentStat> {

        @Override
        public byte getVersion() {
            return 0;
        }

        @Override
        public void encodeValues(Buffer valueBuffer, List<TestAgentStat> agentStats) {
            valueBuffer.putInt(agentStats.size());
            for (TestAgentStat agentStat : agentStats) {
                valueBuffer.putLong(agentStat.getTimestamp());
                valueBuffer.putLong(agentStat.getValue());
            }
        }

        @Override
        public List<TestAgentStat> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
            int size = valueBuffer.readInt();
            List<TestAgentStat> agentStats = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                TestAgentStat agentStat = new TestAgentStat();
                agentStat.setAgentId(decodingContext.getAgentId());
                agentStat.setTimestamp(valueBuffer.readLong());
                agentStat.setValue(valueBuffer.readLong());
                agentStats.add(agentStat);
            }
            return agentStats;
        }
    }

    private static class TestAgentStatEncoder extends AgentStatEncoder<TestAgentStat> {
        protected TestAgentStatEncoder(AgentStatCodec<TestAgentStat> codec) {
            super(codec);
        }
    }

    private static class TestAgentStatDecoder extends AgentStatDecoder<TestAgentStat> {
        protected TestAgentStatDecoder(AgentStatCodec<TestAgentStat> codec) {
            super(Arrays.asList(codec));
        }
    }

    private static class TestAgentStatSerializer extends AgentStatSerializer<TestAgentStat> {
        protected TestAgentStatSerializer(AgentStatEncoder<TestAgentStat> encoder) {
            super(encoder);
        }
    }

    private static class TestAgentStat implements AgentStatDataPoint {

        private String agentId;
        private long startTimestamp;
        private long timestamp;
        private long value;

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

        public long getValue() {
            return this.value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        @Override
        public AgentStatType getAgentStatType() {
            return AGENT_STAT_TYPE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestAgentStat that = (TestAgentStat) o;

            if (startTimestamp != that.startTimestamp) return false;
            if (timestamp != that.timestamp) return false;
            if (value != that.value) return false;
            return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

        }

        @Override
        public int hashCode() {
            int result = agentId != null ? agentId.hashCode() : 0;
            result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
            result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
            result = 31 * result + (int) (value ^ (value >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "TestAgentStat{" +
                    "agentId='" + agentId + '\'' +
                    ", startTimestamp=" + startTimestamp +
                    ", timestamp=" + timestamp +
                    ", value=" + value +
                    '}';
        }
    }
}
