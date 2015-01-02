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
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class MapStatisticsCalleeMapper implements RowMapper<LinkDataMap> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkFilter filter;

    public MapStatisticsCalleeMapper() {
        this(SkipLinkFilter.FILTER);
    }

    public MapStatisticsCalleeMapper(LinkFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        this.filter = filter;
    }

    @Overri    e
	public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        logger.debug("mapRow:{}", rowNum);

        final Buffer row = new FixedBuffer(result.getRow());
        final Application calleeApplication = readCalleeApplication(row);
        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());


        final LinkDataMap linkDataMap = new LinkDataM       p();
		for (KeyValue kv : result.raw()) {

            final byte[] qualifier = kv.getQualifier();
            final Application callerApplication = readCallerApplication(qualifier);
            if (filter.filter(callerApplication)) {
                continue;
            }

            long requestCount = Bytes.toLong(kv.getBuffer(), kv.getValueOffset());
            short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);

            String callerHost = ApplicationMapStatisticsUtils.getHost(qualifier);
            boolean isError = histogramSlot == (short) -1;

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Callee. {} callerHost:{} -> {} (slot:{}/{}),  ", callerApplication, callerHost, calleeApplication, histogramSlot, requestCount);
            }


            final short slotTime = (isError) ? (short) -1 : histogramSlot;
            linkDataMap.addLinkData(callerApplication, callerApplication.getName(), calleeApplication, callerHost, timestamp, slotTime, requestCount);

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Callee. statistics:{}", linkDataMap);
                  }
		}

        re    urn linkDataMap;
	}

    private Application readCallerApplication(byte[] qualifier) {
        String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        return new Application(callerApplicationName, callerServiceType);
    }

    private Application readCalleeApplication(Buffer row) {
        String calleeApplicationName = row.read2PrefixedString();
        short calleeServiceType = row.readShort();
        return new Application(calleeApplicationName, calleeServiceType);
    }
}
