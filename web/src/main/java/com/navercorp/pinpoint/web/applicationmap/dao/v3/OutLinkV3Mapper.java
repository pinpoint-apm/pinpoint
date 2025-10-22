/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.dao.v3;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.TimestampRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
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
public class OutLinkV3Mapper implements RowMapper<LinkDataMap> {

    static final String MERGE_AGENT = "_";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkFilter filter;

    private final ApplicationFactory applicationFactory;

    private final RowKeyDecoder<UidLinkRowKey> rowKeyDecoder;

    private final TimeWindowFunction timeWindowFunction;


    public OutLinkV3Mapper(ApplicationFactory applicationFactory,
                           RowKeyDecoder<UidLinkRowKey> rowKeyDecoder,
                           LinkFilter filter,
                           TimeWindowFunction timeWindowFunction) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");

        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");

        this.filter = Objects.requireNonNull(filter, "filter");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
    }

    @Override
    public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("mapRow num:{} size:{}", rowNum, result.size());
        }

        TimestampRowKey selfRowKey = rowKeyDecoder.decodeRowKey(result.getRow());
        final long timestamp = timeWindowFunction.refineTimestamp(selfRowKey.getTimestamp());

        // key is destApplicationName.
        final LinkDataMap linkDataMap = new LinkDataMap();
        for (Cell cell : result.rawCells()) {
            final Buffer buffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            final Application inApplication = readInApplication(buffer);
            if (filter.filter(inApplication)) {
                continue;
            }

            byte slotCode = buffer.readByte();
            String outSubLink = buffer.readPrefixedString();


            long requestCount = CellUtils.valueToLong(cell);
            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched OUT_LINK {} {} -> {} (slot:{}/{}) inHost:{}",
                        selfRowKey, MERGE_AGENT, inApplication, slotCode, requestCount, outSubLink);
            }

            if (StringUtils.isEmpty(outSubLink)) {
                outSubLink = inApplication.getName();
            }
            Application selfApplication = getApplication(selfRowKey);
            linkDataMap.addLinkDataByCode(selfApplication, MERGE_AGENT, inApplication, outSubLink, timestamp, slotCode, requestCount);
        }

        return linkDataMap;
    }


    private Application readInApplication(Buffer buffer) {
        int inServiceType = buffer.readInt();
        String inApplicationName = buffer.readUnsignedBytePrefixedString();
        return applicationFactory.createApplication(inApplicationName, inServiceType);
    }

    private Application getApplication(RowKey rawRowKey) {
        UidLinkRowKey rowKey = (UidLinkRowKey) rawRowKey;
        String applicationName = rowKey.getApplicationName();
        int outServiceType = rowKey.getServiceType();
        return this.applicationFactory.createApplication(applicationName, outServiceType);
    }

}
