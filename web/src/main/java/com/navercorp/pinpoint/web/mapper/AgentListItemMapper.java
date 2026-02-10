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
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class AgentListItemMapper implements RowMapper<List<AgentListItem>> {

    private final ApplicationFactory applicationFactory;

    public AgentListItemMapper(ApplicationFactory applicationFactory) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @Override
    public List<AgentListItem> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        // parse row
        byte[] row = result.getRow();
        Buffer rowBuffer = new OffsetFixedBuffer(row);
        int serviceUid = rowBuffer.readInt();
        String applicationName = rowBuffer.readPadStringAndRightTrim(254);
        int serviceTypeCode = rowBuffer.readInt();
        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);
        String agentId = rowBuffer.readPadStringAndRightTrim(24);

        // parse cells
        final List<AgentListItem> agentIdList = new ArrayList<>(1);
        for (Cell cell : result.rawCells()) {
            long agentStartTime = LongInverter.restore(ByteArrayUtils.bytesToLong(cell.getQualifierArray(), cell.getQualifierOffset()));
            long lastUpdated = cell.getTimestamp();
            Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            valueBuffer.skip(1); // version
            String agentName = valueBuffer.readPrefixedString();

            AgentListItem agentListItem = new AgentListItem(
                    serviceUid, application,
                    agentId, agentStartTime,
                    lastUpdated,
                    agentName);
            agentIdList.add(agentListItem);
        }
        return agentIdList;
    }
}
