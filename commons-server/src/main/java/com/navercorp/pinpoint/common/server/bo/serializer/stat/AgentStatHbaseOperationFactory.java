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

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentStatHbaseOperationFactory {

    private final AgentStatRowKeyEncoder rowKeyEncoder;

    private final AgentStatRowKeyDecoder rowKeyDecoder;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    @Autowired
    public AgentStatHbaseOperationFactory(
            AgentStatRowKeyEncoder rowKeyEncoder,
            AgentStatRowKeyDecoder rowKeyDecoder,
            @Qualifier("agentStatV2RowKeyDistributor") AbstractRowKeyDistributor rowKeyDistributor) {
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    public <T extends AgentStatDataPoint> List<Put> createPuts(String agentId, AgentStatType agentStatType, List<T> agentStatDataPoints, HbaseSerializer<List<T>, Put> agentStatSerializer) {
        if (CollectionUtils.isEmpty(agentStatDataPoints)) {
            return Collections.emptyList();
        }
        Map<Long, List<T>> timeslots = slotAgentStatDataPoints(agentStatDataPoints);
        List<Put> puts = new ArrayList<Put>();
        for (Map.Entry<Long, List<T>> timeslot : timeslots.entrySet()) {
            long baseTimestamp = timeslot.getKey();
            List<T> slottedAgentStatDataPoints = timeslot.getValue();

            final AgentStatRowKeyComponent rowKeyComponent = new AgentStatRowKeyComponent(agentId, agentStatType, baseTimestamp);
            byte[] rowKey = this.rowKeyEncoder.encodeRowKey(rowKeyComponent);
            byte[] distributedRowKey = this.rowKeyDistributor.getDistributedKey(rowKey);

            Put put = new Put(distributedRowKey);
            agentStatSerializer.serialize(slottedAgentStatDataPoints, put, null);
            puts.add(put);
        }
        return puts;
    }

    public Scan createScan(String agentId, AgentStatType agentStatType, long startTimestamp, long endTimestamp) {
        final AgentStatRowKeyComponent startRowKeyComponent = new AgentStatRowKeyComponent(agentId, agentStatType, AgentStatUtils.getBaseTimestamp(endTimestamp));
        final AgentStatRowKeyComponent endRowKeyComponenet = new AgentStatRowKeyComponent(agentId, agentStatType, AgentStatUtils.getBaseTimestamp(startTimestamp) - HbaseColumnFamily.AGENT_STAT_STATISTICS.TIMESPAN_MS);
        byte[] startRowKey = this.rowKeyEncoder.encodeRowKey(startRowKeyComponent);
        byte[] endRowKey = this.rowKeyEncoder.encodeRowKey(endRowKeyComponenet);
        return new Scan(startRowKey, endRowKey);
    }

    public AbstractRowKeyDistributor getRowKeyDistributor() {
        return this.rowKeyDistributor;
    }

    public String getAgentId(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getAgentId();
    }

    public AgentStatType getAgentStatType(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getAgentStatType();
    }

    public long getBaseTimestamp(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getBaseTimestamp();
    }

    private <T extends AgentStatDataPoint> Map<Long, List<T>> slotAgentStatDataPoints(List<T> agentStatDataPoints) {
        Map<Long, List<T>> timeslots = new TreeMap<Long, List<T>>();
        for (T agentStatDataPoint : agentStatDataPoints) {
            long timestamp = agentStatDataPoint.getTimestamp();
            long timeslot = AgentStatUtils.getBaseTimestamp(timestamp);
            List<T> slottedDataPoints = timeslots.get(timeslot);
            if (slottedDataPoints == null) {
                slottedDataPoints = new ArrayList<T>();
                timeslots.put(timeslot, slottedDataPoints);
            }
            slottedDataPoints.add(agentStatDataPoint);
        }
        return timeslots;
    }
}
