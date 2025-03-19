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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowFunction;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * rowkey = caller col = callee
 *
 * @author netspider
 */
public class OutLinkMapper implements RowMapper<LinkDataMap> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkFilter filter;

    private final ApplicationFactory applicationFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;

    private final TimeWindowFunction timeWindowFunction;


    public OutLinkMapper(ApplicationFactory applicationFactory,
                         RowKeyDistributorByHashPrefix rowKeyDistributor,
                         LinkFilter filter,
                         TimeWindowFunction timeWindowFunction) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");

        this.filter = Objects.requireNonNull(filter, "filter");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
    }

    @Override
    public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        logger.debug("mapRow:{}", rowNum);

        final byte[] rowKey = getOriginalKey(result.getRow());

        final Buffer row = new FixedBuffer(rowKey);
        final Application outApplication = readOutApplication(row);
        final long timestamp = timeWindowFunction.refineTimestamp(TimeUtils.recoveryTimeMillis(row.readLong()));

        // key is destApplicationName.
        final LinkDataMap linkDataMap = new LinkDataMap();
        for (Cell cell : result.rawCells()) {
            final Buffer buffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            final Application inApplication = readInApplication(buffer);
            if (filter.filter(inApplication)) {
                continue;
            }

            String inHost = buffer.readPrefixedString();
            short histogramSlot = buffer.readShort();

            boolean isError = histogramSlot == (short) -1;

            String outAgentId = buffer.readPrefixedString();

            long requestCount = CellUtils.valueToLong(cell);
            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched {}.(New) {} {} -> {} (slot:{}/{}) inHost:{}",
                        LinkDirection.OUT_LINK, outApplication, outAgentId, inApplication, histogramSlot, requestCount, inHost);
            }

            final short slotTime = (isError) ? (short) -1 : histogramSlot;
            if (StringUtils.isEmpty(inHost)) {
                inHost = inApplication.getName();
            }
            linkDataMap.addLinkData(outApplication, outAgentId, inApplication, inHost, timestamp, slotTime, requestCount);
        }

        return linkDataMap;
    }


    private Application readInApplication(Buffer buffer) {
        short inServiceType = buffer.readShort();
        String inApplicationName = buffer.readPrefixedString();
        return applicationFactory.createApplication(inApplicationName, inServiceType);
    }

    private Application readOutApplication(Buffer row) {
        String applicationName = row.read2PrefixedString();
        short outServiceType = row.readShort();
        return this.applicationFactory.createApplication(applicationName, outServiceType);
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributor.getOriginalKey(rowKey);
    }
}
