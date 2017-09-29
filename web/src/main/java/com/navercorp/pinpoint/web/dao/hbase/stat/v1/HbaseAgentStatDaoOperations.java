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

package com.navercorp.pinpoint.web.dao.hbase.stat.v1;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV1;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.Range;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.springframework.stereotype.Component;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
@Deprecated
@Component
public class HbaseAgentStatDaoOperations {
    private static final long USE_AGGREGATED_THRESHOLD = TimeUnit.HOURS.toMillis(24);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

    private int scanCacheSize = 256;

    void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }

    <T extends AgentStatDataPoint> List<T> getAgentStatList(AgentStatMapperV1<T> mapper, Aggregator<T> aggregator, String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("scanAgentStat : agentId={}, {}", agentId, range);
        }

//        boolean useAggregated = range.getRange() > USE_AGGREGATED_THRESHOLD;
//        if (useAggregated) {
//            return getAggregatedAgentStatList(mapper, aggregator, agentId, range);
//        } else {
        return getAgentStatListFromRaw(mapper, agentId, range);
//        }
    }

    private <T extends AgentStatDataPoint> List<T> getAgentStatListFromRaw(AgentStatMapperV1<T> mapper, String agentId, Range range) {
        Scan scan = createScan(agentId, range);

        List<List<T>> intermediate = hbaseOperations2.find(HBaseTables.AGENT_STAT, scan, rowKeyDistributor, mapper);

        int expectedSize = (int) (range.getRange() / 5000); // data for 5 seconds
        List<T> merged = new ArrayList<>(expectedSize);

        for (List<T> each : intermediate) {
            merged.addAll(each);
        }

        return merged;
    }


    <T extends AgentStatDataPoint> boolean agentStatExists(AgentStatMapperV1<T> mapper, String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("checking for stat data existence : agentId={}, {}", agentId, range);
        }

        Scan scan = createScan(agentId, range);
        scan.setCaching(1);

        AgentStatDataExistsResultsExtractor<T> extractor = new AgentStatDataExistsResultsExtractor(mapper);
        return hbaseOperations2.find(HBaseTables.AGENT_STAT, scan, rowKeyDistributor, extractor);
    }

    <T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> List<S> getSampledAgentStatList(SampledAgentStatResultExtractor<T, S> resultExtractor, String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (resultExtractor == null) {
            throw new NullPointerException("sampledResultExtractor must not be null");
        }
        Scan scan = createScan(agentId, range);
        return hbaseOperations2.find(HBaseTables.AGENT_STAT, scan, rowKeyDistributor, resultExtractor);
    }

    private class AgentStatDataExistsResultsExtractor<T extends AgentStatDataPoint>  implements ResultsExtractor<Boolean> {

        private final RowMapper<List<T>> agentStatMapper;

        private AgentStatDataExistsResultsExtractor(AgentStatMapperV1<T> agentStatMapper) {
            this.agentStatMapper = agentStatMapper;
        }

        @Override
        public Boolean extractData(ResultScanner results) throws Exception {
            int matchCnt = 0;
            for (Result result : results) {
                if (!result.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("stat data exists, most recent data : {}", this.agentStatMapper.mapRow(result, matchCnt++));
                    }
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * make a row key based on timestamp
     * FIXME there is the same duplicate code at collector's dao module
     */
    private byte[] getRowKey(String agentId, long timestamp) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not null");
        }
        byte[] bAgentId = BytesUtils.toBytes(agentId);
        return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
    }

    private Scan createScan(String agentId, Range range) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);

        byte[] startKey = getRowKey(agentId, range.getFrom() - 1);
        byte[] endKey = getRowKey(agentId, range.getTo());

        // start key is replaced by end key because key has been reversed
        scan.setStartRow(endKey);
        scan.setStopRow(startKey);

        //        scan.addColumn(HBaseTables.AGENT_STAT_CF_STATISTICS, HBaseTables.AGENT_STAT_CF_STATISTICS_V1);
        scan.addFamily(HBaseTables.AGENT_STAT_CF_STATISTICS);
        scan.setId("AgentStatScan");

        // toString() method of Scan converts a message to json format so it is slow for the first time.
        logger.debug("create scan:{}", scan);
        return scan;
    }
}