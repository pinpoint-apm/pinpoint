/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.ApplicationStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;

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
 * @author Minwoo Jung
 */
@Component
public class ApplicationStatHbaseOperationFactory {

    private final ApplicationStatRowKeyEncoder rowKeyEncoder;

    private final ApplicationStatRowKeyDecoder rowKeyDecoder;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    @Autowired
    public ApplicationStatHbaseOperationFactory(
                ApplicationStatRowKeyEncoder rowKeyEncoder,
                ApplicationStatRowKeyDecoder rowKeyDecoder,
                @Qualifier("applicationStatRowKeyDistributor") AbstractRowKeyDistributor rowKeyDistributor) {
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    public List<Put> createPuts(String applicationId, List<JoinStatBo> joinStatBoList, StatType statType, ApplicationStatSerializer applicationStatSerializer) {
        if (CollectionUtils.isEmpty(joinStatBoList)) {
            return Collections.emptyList();
        }

        Map<Long, List<JoinStatBo>> timeslots = slotApplicationStatDataPoints(joinStatBoList);
        List<Put> puts = new ArrayList<Put>();
        for (Map.Entry<Long, List<JoinStatBo>> timeslot : timeslots.entrySet()) {
            long baseTimestamp = timeslot.getKey();
            List<JoinStatBo> slottedApplicationStatDataPoints = timeslot.getValue();

            final ApplicationStatRowKeyComponent rowKeyComponent = new ApplicationStatRowKeyComponent(applicationId, statType, baseTimestamp);
            byte[] rowKey = this.rowKeyEncoder.encodeRowKey(rowKeyComponent);
            byte[] distributedRowKey = this.rowKeyDistributor.getDistributedKey(rowKey);

            Put put = new Put(distributedRowKey);
            applicationStatSerializer.serialize(slottedApplicationStatDataPoints, put, null);
            puts.add(put);
        }
        return puts;
    }

    public Scan createScan(String applicationId, StatType statType, long startTimestamp, long endTimestamp) {
        final ApplicationStatRowKeyComponent startRowKeyComponent = new ApplicationStatRowKeyComponent(applicationId, statType, AgentStatUtils.getBaseTimestamp(endTimestamp));
        final ApplicationStatRowKeyComponent endRowKeyComponenet = new ApplicationStatRowKeyComponent(applicationId, statType, AgentStatUtils.getBaseTimestamp(startTimestamp) - HbaseColumnFamily.APPLICATION_STAT_STATISTICS.TIMESPAN_MS);
        byte[] startRowKey = this.rowKeyEncoder.encodeRowKey(startRowKeyComponent);
        byte[] endRowKey = this.rowKeyEncoder.encodeRowKey(endRowKeyComponenet);
        return new Scan(startRowKey, endRowKey);
    }

    public AbstractRowKeyDistributor getRowKeyDistributor() {
        return this.rowKeyDistributor;
    }

    public String getApplicationId(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getApplicationId();
    }

    public long getBaseTimestamp(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getBaseTimestamp();
    }

    private <T extends JoinStatBo> Map<Long, List<T>> slotApplicationStatDataPoints(List<T> joinStatBoList) {
        Map<Long, List<T>> timeslots = new TreeMap<Long, List<T>>();
        for (T joinStatBo : joinStatBoList) {
            long timestamp = joinStatBo.getTimestamp();
            long timeslot = AgentStatUtils.getBaseTimestamp(timestamp);
            List<T> slottedDataPoints = timeslots.get(timeslot);
            if (slottedDataPoints == null) {
                slottedDataPoints = new ArrayList<T>();
                timeslots.put(timeslot, slottedDataPoints);
            }
            slottedDataPoints.add(joinStatBo);
        }
        return timeslots;
    }
}
