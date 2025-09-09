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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.util.UserNodeUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 *
 * @author emeroad
 * @author netspider
 * 
 */
public class InLinkMapper implements RowMapper<LinkDataMap> {

    static final String MERGE_AGENT = OutLinkMapper.MERGE_AGENT;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkFilter filter;

    private final ServiceTypeRegistryService registry;

    private final ApplicationFactory applicationFactory;

    private final TimeWindowFunction timeWindowFunction;

    private final int saltKeySize;

    public InLinkMapper(ServiceTypeRegistryService registry,
                        ApplicationFactory applicationFactory,
                        RowKeyDistributorByHashPrefix rowKeyDistributor,
                        LinkFilter filter,
                        TimeWindowFunction timeWindowFunction) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.saltKeySize = rowKeyDistributor.getSaltKeySize();

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

        final byte[] rowKey = result.getRow();

        final Buffer row = new FixedBuffer(rowKey);
        row.setOffset(saltKeySize);
        final Application inApplication = readInApplication(row);
        final long timestamp = timeWindowFunction.refineTimestamp(TimeUtils.recoveryTimeMillis(row.readLong()));

        final LinkDataMap linkDataMap = new LinkDataMap(timeWindowFunction);
        for (Cell cell : result.rawCells()) {

            final byte[] qualifier = CellUtil.cloneQualifier(cell);

            final Buffer buffer = new FixedBuffer(qualifier);
            short selfServiceType = buffer.readShort();
            short histogramSlot = buffer.readShort();
            String selfApplicationName = buffer.read2PrefixedString();

            final Application self = readSelfApplication(selfApplicationName, selfServiceType, inApplication.getServiceType());
            if (filter.filter(self)) {
                continue;
            }

            long requestCount = CellUtils.valueToLong(cell);
            String selfHost = readOutHost(buffer);
            // There may be no outHost for virtual queue nodes from user-defined entry points.
            // Terminal nodes, such as httpclient will not have outHost set as well, but since they're terminal
            // nodes, they would not have reached here in the first place.
            if (inApplication.getServiceType().isQueue()) {
                selfHost = Objects.toString(selfHost, "");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched IN_LINK {} outHost:{} -> {} (slot:{}/{})",
                        self, selfHost, inApplication, histogramSlot, requestCount);
            }

            linkDataMap.addLinkData(self, self.getName(), inApplication, selfHost, timestamp, histogramSlot, requestCount);

            if (logger.isTraceEnabled()) {
                logger.trace("    Fetched IN_LINK inLink:{}", linkDataMap);
            }
        }

        return linkDataMap;
    }

    private String readOutHost(Buffer buffer) {
        String outHost = buffer.readPadStringAndRightTrim(buffer.remaining());
        if (MERGE_AGENT.equals(outHost)) {
            return MERGE_AGENT;
        }
        return outHost;
    }

    private Application readSelfApplication(String selfApplicationName, short selfServiceType, ServiceType inServiceType) {
        // Caller may be a user node, and user nodes may call nodes with the same application name but different service type.
        // To distinguish between these user nodes, append callee's service type to the application name.
        if (registry.findServiceType(selfServiceType).isUser()) {
            selfApplicationName = UserNodeUtils.newUserNodeName(selfApplicationName, inServiceType);
        }
        return this.applicationFactory.createApplication(selfApplicationName, selfServiceType);
    }

    private Application readInApplication(Buffer row) {
        String selfApplicationName = row.read2PrefixedString();
        short selfServiceType = row.readShort();

        return this.applicationFactory.createApplication(selfApplicationName, selfServiceType);
    }

}
