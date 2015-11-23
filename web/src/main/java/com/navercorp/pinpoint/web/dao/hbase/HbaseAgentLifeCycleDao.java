/*
 * Copyright 2015 NAVER Corp.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.common.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentLifeCycleDao implements AgentLifeCycleDao {
    
    private static final int SCAN_CACHING_SIZE = 20;
    private static final int NUM_LIFE_CYCLES_TO_MATCH = 1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("agentLifeCycleMapper")
    private RowMapper<AgentLifeCycleBo> agentLifeCycleMapper;

    @Override
    public AgentLifeCycleBo getAgentLifeCycle(String agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        Scan scan = new Scan();
        scan.setMaxVersions(1);
        scan.setCaching(SCAN_CACHING_SIZE);

        long fromTime = TimeUtils.reverseTimeMillis(timestamp);

        byte[] agentIdBytes = Bytes.toBytes(agentId);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, fromTime);
        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);

        scan.setStartRow(startKeyBytes);
        scan.setStopRow(endKeyBytes);
        scan.addColumn(HBaseTables.AGENT_LIFECYCLE_CF_STATUS, HBaseTables.AGENT_LIFECYCLE_CF_STATUS_QUALI_STATES);

        try {
            List<AgentLifeCycleBo> agentLifeCycles = this.hbaseOperations2.find(HBaseTables.AGENT_LIFECYCLE, scan, new AgentLifeCycleResultsExtractor(timestamp));
            if (agentLifeCycles.isEmpty()) {
                logger.debug("agentLifeCycle not found for agentId={}, timestamp={}", agentId, timestamp);
                return null;
            }
            
            AgentLifeCycleBo latestLifeCycle = agentLifeCycles.get(0);
            logger.debug("agentLifeCycle found for agentId={}, timestamp={}, value={}", agentId, timestamp, latestLifeCycle);
            return latestLifeCycle;
        } catch (Exception e) {
            logger.warn("could not retrieve agentLifeCycle for agentId={}, timestamp={}", agentId, timestamp);
            return null;
        }
    }

    private class AgentLifeCycleResultsExtractor implements ResultsExtractor<List<AgentLifeCycleBo>> {
        
        private final long timestamp;
        
        private AgentLifeCycleResultsExtractor(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public List<AgentLifeCycleBo> extractData(ResultScanner results) throws Exception {
            int found = 0;
            int matchCnt = 0;
            List<AgentLifeCycleBo> agentLifeCycles = new ArrayList<>();
            for (Result result : results) {
                AgentLifeCycleBo agentLifeCycle = agentLifeCycleMapper.mapRow(result, found++);
                if (agentLifeCycle.getEventTimestamp() < timestamp) {
                    agentLifeCycles.add(agentLifeCycle);
                    ++matchCnt;
                }
                if (matchCnt >= NUM_LIFE_CYCLES_TO_MATCH) {
                    break;
                }
            }
            return agentLifeCycles;
        }

    }

}
