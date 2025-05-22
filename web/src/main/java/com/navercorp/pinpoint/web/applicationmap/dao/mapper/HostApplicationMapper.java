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
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class HostApplicationMapper implements ResultsExtractor<Set<AcceptApplication>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationFactory applicationFactory;

    public HostApplicationMapper(ApplicationFactory applicationFactory) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @Override
    public Set<AcceptApplication> extractData(ResultScanner results) throws Exception {
        Set<AcceptApplication> applicationSet = new HashSet<>(16);
        for (Result result : results) {
            mapRow(result, applicationSet);
        }
        return applicationSet;
    }


    private void mapRow(Result result, Set<AcceptApplication> acceptApplicationSet) throws Exception {
        if (result.isEmpty()) {
            return;
        }
        for (Cell cell : result.rawCells()) {
            AcceptApplication acceptedApplication = createAcceptedApplication(cell);
            acceptApplicationSet.add(acceptedApplication);
        }
    }

//    private void readRowKey(byte[] rowKey) {
//        final Buffer rowKeyBuffer= new FixedBuffer(rowKey);
//        final String parentApplicationName = rowKeyBuffer.readPadStringAndRightTrim(HBaseTableConstants.APPLICATION_NAME_MAX_LEN);
//        final short parentApplicationServiceType = rowKeyBuffer.readShort();
//        final long timeSlot = TimeUtils.recoveryTimeMillis(rowKeyBuffer.readLong());
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("parentApplicationName:{}/{} time:{}", parentApplicationName, parentApplicationServiceType, timeSlot);
//        }
//    }

    private AcceptApplication createAcceptedApplication(Cell cell) {
        Buffer reader = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
        String host = reader.readPrefixedString();
        String bindApplicationName = reader.readPrefixedString();
        short bindServiceTypeCode = reader.readShort();

        final Application bindApplication = applicationFactory.createApplication(bindApplicationName, bindServiceTypeCode);
        return new AcceptApplication(host, bindApplication);
    }
}
