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
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class HostApplicationMapperVer2 implements RowMapper<List<AcceptApplication>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationFactory applicationFactory;

    @Override
    public List<AcceptApplication> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
//       readRowKey(result.getRow());

        final List<AcceptApplication> acceptApplicationList = new ArrayList<>(result.size());
        for (Cell cell : result.rawCells()) {
            AcceptApplication acceptedApplication = createAcceptedApplication(cell);
            acceptApplicationList.add(acceptedApplication);
        }
        return acceptApplicationList;
    }

//    private void readRowKey(byte[] rowKey) {
//        final Buffer rowKeyBuffer= new FixedBuffer(rowKey);
//        final String parentApplicationName = rowKeyBuffer.readPadStringAndRightTrim(HBaseTables.APPLICATION_NAME_MAX_LEN);
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
