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
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author emeroad
 */
@Component
public class ResponseTimeMapper implements RowMapper<ResponseTime> {
    @Autowired
    private ServiceTypeRegistryService registry;

    @Override
    public ResponseTime mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        final byte[] rowKey = result.getRow();
        ResponseTime responseTime = createResponseTime(rowKey);

        for (KeyValue keyValue : result.raw()) {
            if (!Bytes.equals(keyValue.getFamily(), HBaseTables.MAP_STATISTICS_SELF_CF_COUNTER)) {
                continue;
            }
            byte[] qualifier = keyValue.getQualifier();

            recordColumn(responseTime, qualifier, keyValue.getBuffer(), keyValue.getValueOffset());
        }
        return responseTime;
    }



    void recordColumn(ResponseTime responseTime, byte[] qualifier, byte[] value, int valueOffset) {
        short slotNumber = Bytes.toShort(qualifier);
        // agentId should be added as data.
        String agentId = Bytes.toString(qualifier, 2, qualifier.length - 2);
        long count = Bytes.toLong(value, valueOffset);
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

}
