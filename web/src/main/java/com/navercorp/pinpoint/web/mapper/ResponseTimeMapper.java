/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * @author emeroad
 */
@Component
public class ResponseTimeMapper implements RowMapper<ResponseTime> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceTypeRegistryService registry;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public ResponseTimeMapper(ServiceTypeRegistryService registry, @Qualifier("statisticsSelfRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public ResponseTime mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        final byte[] rowKey = getOriginalKey(result.getRow());

        ResponseTime responseTime = createResponseTime(rowKey);
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER.getName())) {
                recordColumn(responseTime, cell);
            }

            if (logger.isTraceEnabled()) {
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                logger.trace("unknown column family:{}", columnFamily);
            }
        }
        return responseTime;
    }

    void recordColumn(ResponseTime responseTime, Cell cell) {

        final byte[] qArray = cell.getQualifierArray();
        final int qOffset = cell.getQualifierOffset();
        short slotNumber = Bytes.toShort(qArray, qOffset);

        // agentId should be added as data.
        String agentId = Bytes.toString(qArray, qOffset + BytesUtils.SHORT_BYTE_LENGTH, cell.getQualifierLength() - BytesUtils.SHORT_BYTE_LENGTH);
        long count = Bytes.toLong(cell.getValueArray(), cell.getValueOffset());
        responseTime.addResponseTime(agentId, slotNumber, count);
    }

    private ResponseTime createResponseTime(byte[] rowKey) {
        final Buffer row = new FixedBuffer(rowKey);
        String applicationName = row.read2PrefixedString();
        short serviceTypeCode = row.readShort();
        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return new ResponseTime(applicationName, serviceType, timestamp);
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}
