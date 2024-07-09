/*
 * Copyright 2024 NAVER Corp.
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
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author intr3p1d
 */
@Component
public class ApplicationMapInboundTimeAggregatedMapper implements RowMapper<LinkDataMap> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkFilter filter;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private ApplicationFactory applicationFactory;

    @Autowired
    @Qualifier("applicationMapInboundRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public ApplicationMapInboundTimeAggregatedMapper() {
        this(LinkFilter::skip);
    }

    public ApplicationMapInboundTimeAggregatedMapper(LinkFilter filter) {
        this.filter = filter;
    }

    @Override
    public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        logger.debug("mapRow: {}", rowNum);

        final byte[] rowKey = getOriginalKey(result.getRow());

        final Buffer row = new FixedBuffer(rowKey);
        final Application destApplication = readDestApplication(row);
        final long timestamp = 0; // time aggregated

        final LinkDataMap linkDataMap = new LinkDataMap();
        for (Cell cell : result.rawCells()) {

            final byte[] qualifier = CellUtil.cloneQualifier(cell);
            final Application srcApplication = readSourceApplication(qualifier, destApplication.getServiceType());
            if (filter.filter(srcApplication)) {
                continue;
            }

            long requestCount = CellUtils.valueToLong(cell);
            short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);

            String srcHost = srcApplication.getName();
            String destHost = destApplication.getName();

            boolean isError = histogramSlot == (short) -1;

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched {}. {} srcHost:{} -> {} (slot:{}/{}),  ", LinkDirection.IN_LINK, srcApplication, srcHost, destApplication, histogramSlot, requestCount);
            }

            final short slotTime = (isError) ? (short) -1 : histogramSlot;
            linkDataMap.addLinkData(srcApplication, srcApplication.getName(), destApplication, srcHost, timestamp, slotTime, requestCount);

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched {}. statistics:{}", LinkDirection.IN_LINK, linkDataMap);
            }
        }

        return linkDataMap;
    }

    private Application readSourceApplication(byte[] qualifier, ServiceType destServiceType) {
        short srcServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        // Caller may be a user node, and user nodes may call nodes with the same application name but different service type.
        // To distinguish between these user nodes, append callee's service type to the application name.
        String srcApplicationName;
        if (registry.findServiceType(srcServiceType).isUser()) {
            srcApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnNameForUser(qualifier, destServiceType);
        } else {
            srcApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        }
        return applicationFactory.createApplication(srcApplicationName, srcServiceType);
    }

    private Application readDestApplication(Buffer row) {
        String serviceName = row.read2PrefixedString();
        String applicationName = row.read2PrefixedString();
        short serviceType = row.readShort();
        return applicationFactory.createApplication(applicationName, serviceType);
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }

}
