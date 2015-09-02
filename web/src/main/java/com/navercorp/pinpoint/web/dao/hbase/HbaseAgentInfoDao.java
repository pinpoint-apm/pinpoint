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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;

import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.stereotype.Repository;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private ServiceTypeRegistryService registry;

    /**
     * Returns the information of the agent with its start time closest to the given timestamp
     *
     * @param agentId
     * @param timestamp
     * @return
     */
    @Override
    public AgentInfoBo getAgentInfo(final String agentId, final long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        // TODO need to be cached
        Scan scan = createScan(agentId, timestamp);
        scan.setMaxVersions(1);
        scan.setCaching(1);
        
        AgentInfoBo result = this.hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new AgentInfoBoResultsExtractor(agentId));
        if (result == null) {
            logger.warn("agentInfo not found. agentId={}, time={}", agentId, timestamp);
        }
        return result;
    }
    
    /**
     * Returns the very first information of the agent
     *
     * @param agentId
     */
    @Override
    public AgentInfoBo getInitialAgentInfo(final String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        Scan scan = new Scan();
        byte[] agentIdBytes = Bytes.toBytes(agentId);
        byte[] reverseStartKey = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);
        scan.setStartRow(reverseStartKey);
        scan.setReversed(true);
        scan.setMaxVersions(1);
        scan.setCaching(1);
        
        AgentInfoBo result = this.hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new AgentInfoBoResultsExtractor(agentId));
        if (result == null) {
            logger.warn("agentInfo not found. agentId={}, time={}", agentId, 0);
        }
        return result;
    }

    private Scan createScan(String agentId, long currentTime) {
        Scan scan = new Scan();

        byte[] agentIdBytes = Bytes.toBytes(agentId);
        long startTime = TimeUtils.reverseTimeMillis(currentTime);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, startTime);
        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);
        
        scan.setStartRow(startKeyBytes);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(HBaseTables.AGENTINFO_CF_INFO);

        return scan;
    }
    
    private class AgentInfoBoResultsExtractor implements ResultsExtractor<AgentInfoBo> {
        
        private final String agentId;
        
        private AgentInfoBoResultsExtractor(String agentId) {
            this.agentId = agentId;
        }

        @Override
        public AgentInfoBo extractData(ResultScanner results) throws Exception {
            for (Result next : results) {
                byte[] row = next.getRow();
                long reverseStartTime = BytesUtils.bytesToLong(row, HBaseTables.AGENT_NAME_MAX_LEN);
                long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);
                
                byte[] serializedAgentInfo = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
                byte[] serializedServerMetaData = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA);

                final AgentInfoBo.Builder agentInfoBoBuilder = createBuilderFromValue(serializedAgentInfo);
                agentInfoBoBuilder.setAgentId(this.agentId);
                agentInfoBoBuilder.setStartTime(startTime);
                // TODO fix
                agentInfoBoBuilder.setServiceType(registry.findServiceType(agentInfoBoBuilder.getServiceTypeCode()));
                if (serializedServerMetaData != null) {
                    agentInfoBoBuilder.setServerMetaData(new ServerMetaDataBo.Builder(serializedServerMetaData).build());
                }
                final AgentInfoBo agentInfoBo = agentInfoBoBuilder.build();

                logger.debug("agent:{} startTime value {}", agentId, startTime);

                return agentInfoBo;
            }

            return null;
        }
        
        private AgentInfoBo.Builder createBuilderFromValue(byte[] serializedAgentInfo) {
            final Buffer buffer = new FixedBuffer(serializedAgentInfo);
            final AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
            builder.setHostName(buffer.readPrefixedString());
            builder.setIp(buffer.readPrefixedString());
            builder.setPorts(buffer.readPrefixedString());
            builder.setApplicationName(buffer.readPrefixedString());
            builder.setServiceTypeCode(buffer.readShort());
            builder.setPid(buffer.readInt());
            builder.setAgentVersion(buffer.readPrefixedString());
            builder.setStartTime(buffer.readLong());
            builder.setEndTimeStamp(buffer.readLong());
            builder.setEndStatus(buffer.readInt());
            // FIXME - 2015.09 v1.5.0 added vmVersion (check for compatibility)
            if (buffer.limit() > 0) {
                builder.setVmVersion(buffer.readPrefixedString());
            }
            return builder;
        }
        
    }
}
