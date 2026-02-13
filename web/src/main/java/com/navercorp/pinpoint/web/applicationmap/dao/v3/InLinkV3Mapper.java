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

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.util.UserNodeUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.SlotCode;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Predicate;

/**
 *
 * @author emeroad
 * @author netspider
 * 
 */
public class InLinkV3Mapper implements RowMapper<LinkDataMap> {

    static final String MERGE_AGENT = OutLinkV3Mapper.MERGE_AGENT;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkFilter filter;

    private final ServiceTypeRegistryService registry;

    private final ApplicationFactory applicationFactory;

    private final RowKeyDecoder<UidLinkRowKey> rowKeyDecoder;

    private final TimeWindowFunction timeWindowFunction;

    private final Predicate<UidLinkRowKey> rowFilter;


    public InLinkV3Mapper(ServiceTypeRegistryService registry,
                          ApplicationFactory applicationFactory,
                          RowKeyDecoder<UidLinkRowKey> rowKeyDecoder,
                          LinkFilter filter,
                          TimeWindowFunction timeWindowFunction,
                          Predicate<UidLinkRowKey> rowFilter) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");

        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");

        this.filter = Objects.requireNonNull(filter, "filter");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
        this.rowFilter = Objects.requireNonNull(rowFilter, "rowFilter");
    }

    @Override
    public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("mapRow num:{} size:{}", rowNum, result.size());
        }

        final LinkDataMap linkDataMap = new LinkDataMap(timeWindowFunction);
        for (Cell cell : result.rawCells()) {
            final UidLinkRowKey inRowKey = rowKeyDecoder.decodeRowKey(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
            if (!rowFilter.test(inRowKey)) {
                continue;
            }

            final long timestamp = timeWindowFunction.refineTimestamp(inRowKey.getTimestamp());
            final Application inApplication = readInApplication(inRowKey);

            final Application self = readSelfApplication(inRowKey, inApplication.getServiceType());
            if (filter.filter(self)) {
                continue;
            }

            SlotCode slotCode = readSlotCode(cell);

            long requestCount = CellUtils.valueToLong(cell);
            String selfHost = outHost(inRowKey.getSubLink());
            // There may be no outHost for virtual queue nodes from user-defined entry points.
            // Terminal nodes, such as httpclient will not have outHost set as well, but since they're terminal
            // nodes, they would not have reached here in the first place.
            if (inApplication.getServiceType().isQueue()) {
                selfHost = Objects.toString(selfHost, "");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched IN_LINK {} outHost:{} -> {} (slot:{}/{})",
                        self, selfHost, inApplication, slotCode, requestCount);
            }

            linkDataMap.addLinkDataByCode(self, self.getName(), inApplication, selfHost, timestamp, slotCode, requestCount);

            if (logger.isTraceEnabled()) {
                logger.trace("    Fetched IN_LINK inLink:{}", linkDataMap);
            }
        }

        return linkDataMap;
    }


    private String outHost(String outHost) {
        if (MERGE_AGENT.equals(outHost)) {
            return MERGE_AGENT;
        }
        return outHost;
    }

    private SlotCode readSlotCode(Cell cell) {
        byte[] qualifierArray = cell.getQualifierArray();
        byte slotCodeByte = qualifierArray[cell.getQualifierOffset()];
        return SlotCode.valueOf(slotCodeByte);
    }

    private Application readSelfApplication(UidLinkRowKey self, ServiceType inServiceType) {
        int serviceUid = self.getServiceUid();
        String selfApplicationName = self.getLinkApplicationName();
        int selfServiceType = self.getLinkServiceType();

        // Caller may be a user node, and user nodes may call nodes with the same application name but different service type.
        // To distinguish between these user nodes, append callee's service type to the application name.
        if (registry.findServiceType(selfServiceType).isUser()) {
            selfApplicationName = UserNodeUtils.newUserNodeName(selfApplicationName, inServiceType);
        }
        return this.applicationFactory.createApplication(serviceUid, selfApplicationName, selfServiceType);
    }

    private Application readInApplication(UidRowKey rowKey) {
        int serviceUid = rowKey.getServiceUid();
        String selfApplicationName = rowKey.getApplicationName();
        int selfServiceType = rowKey.getServiceType();

        return this.applicationFactory.createApplication(serviceUid, selfApplicationName, selfServiceType);
    }

}
