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
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * rowkey = caller col = callee
 *
 * @author netspider
 */
@Component
public class MapStatisticsCallerMapper implements RowMapper<LinkDataMap> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkFilter filter;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private ApplicationFactory applicationFactory;

    @Autowired
    @Qualifier("statisticsCallerRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public MapStatisticsCallerMapper() {
        this(SkipLinkFilter.FILTER);
    }

    public MapStatisticsCallerMapper(LinkFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        this.filter = filter;
    }

    @Override
    public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        logger.debug("mapRow:{}", rowNum);

        final byte[] rowKey = getOriginalKey(result.getRow());

        final Buffer row = new FixedBuffer(rowKey);
        final Application caller = readCallerApplication(row);
        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());

        // key is destApplicationName.
        final LinkDataMap linkDataMap = new LinkDataMap();
        for (Cell cell : result.rawCells()) {
            final Buffer buffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            final Application callee = readCalleeApplication(buffer);
            if (filter.filter(callee)) {
                continue;
            }

            String calleeHost = buffer.readPrefixedString();
            short histogramSlot = buffer.readShort();

            boolean isError = histogramSlot == (short) -1;

            String callerAgentId = buffer.readPrefixedString();

            long requestCount = getValueToLong(cell);
            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Caller.(New) {} {} -> {} (slot:{}/{}) calleeHost:{}", caller, callerAgentId, callee, histogramSlot, requestCount, calleeHost);
            }

            final short slotTime = (isError) ? (short) -1 : histogramSlot;
            if (StringUtils.isEmpty(calleeHost)) {
                calleeHost = callee.getName();
            }
            linkDataMap.addLinkData(caller, callerAgentId, callee, calleeHost, timestamp, slotTime, requestCount);
        }

        return linkDataMap;
    }

    private long getValueToLong(Cell cell) {
        return Bytes.toLong(cell.getValueArray(), cell.getValueOffset());
    }

    private Application readCalleeApplication(Buffer buffer) {
        short calleeServiceType = buffer.readShort();
        String calleeApplicationName = buffer.readPrefixedString();
        return applicationFactory.createApplication(calleeApplicationName, calleeServiceType);
    }

    private Application readCallerApplication(Buffer row) {
        String callerApplicationName = row.read2PrefixedString();
        short callerServiceType = row.readShort();
        return this.applicationFactory.createApplication(callerApplicationName, callerServiceType);
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}
