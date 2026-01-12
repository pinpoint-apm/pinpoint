/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.scatter.dao.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowTypeHint;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 * @author netspider
 */
@Component
public class TransactionIdMapper implements RowMapper<List<ServerTraceId>>, RowTypeHint {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily traceIndex;

    public TransactionIdMapper() {
        this.traceIndex = HbaseTables.APPLICATION_TRACE_INDEX_TRACE;
    }

    @Override
    public List<ServerTraceId> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        Cell[] rawCells = result.rawCells();
        List<ServerTraceId> traceIdList = new ArrayList<>(rawCells.length);
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, traceIndex.getName())) {
                ServerTraceId serverTraceId = ServerTraceId.of(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                traceIdList.add(serverTraceId);
                logger.debug("found traceId {}", serverTraceId);
            }
        }
        return traceIdList;
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
