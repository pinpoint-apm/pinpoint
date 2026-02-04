/*
 * Copyright 2026 NAVER Corp.
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
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.util.AgentIdRowKeyUtils;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class AgentStartTimeInfoMapper implements RowMapper<List<AgentIdEntry>> {

    private final ApplicationFactory applicationFactory;
    private final Predicate<byte[]> rowFilter;

    public AgentStartTimeInfoMapper(ApplicationFactory applicationFactory, Predicate<byte[]> rowFilter) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.rowFilter = rowFilter;
    }

    @Override
    public List<AgentIdEntry> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        byte[] row = result.getRow();
        if (rowFilter != null && !rowFilter.test(row)) {
            return Collections.emptyList();
        }

        // parse row
        int serviceUid = AgentIdRowKeyUtils.extractServiceUid(row);
        String applicationName = AgentIdRowKeyUtils.extractApplicationName(row);
        int serviceTypeCode = AgentIdRowKeyUtils.extractServiceTypeCode(row);
        Application application = applicationFactory.createApplication(serviceUid, applicationName, serviceTypeCode);

        String agentId = AgentIdRowKeyUtils.extractAgentId(row);
        long agentStartTime = AgentIdRowKeyUtils.extractAgentStartTime(row);

        // parse cell
        Cell cell = result.getColumnLatestCell(HbaseTables.AGENT_ID.getName(), HbaseTables.AGENT_ID.getName());
        Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        valueBuffer.skip(1); // version
        String agentName = valueBuffer.readPrefixedString();

        // TODO: create update time column and use it instead of latest timestamp of cells
        long latestTimestamp = getLatestTimestamp(result);
        AgentIdEntry agentIdEntry = new AgentIdEntry(application,
                agentId, agentStartTime,
                latestTimestamp,
                agentName);
        return List.of(agentIdEntry);
    }

    private long getLatestTimestamp(Result result) {
        long latestTimestamp = Long.MIN_VALUE;
        for (Cell cell : result.rawCells()) {
            long ts = cell.getTimestamp();
            if (ts > latestTimestamp) {
                latestTimestamp = ts;
            }
        }
        return latestTimestamp;
    }
}
